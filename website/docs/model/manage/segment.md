---
title: Segment
language: en
sidebar_label: Segment
pagination_label: Segment
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - segment merge
draft: false
last_update:
    date: 08/19/2022
---

Model (index group) consists of one or more segments. Each segment contains a range of data. Segment is created by building index or loading data with a selected data range on the partition columns.

The main contents are as follows:


### <span id="view">View Segment</span>

User can access the Segment management interface by following these steps:

1. Open **Data Asset -> Model** page, and click the model (index group) name.
2. Select **Segment** tag.

The model list page in the project is shown as below.

![segment](images/segment/segment.png)

Field description of the Segment list:

- Start time: The start time of the data in the Segment. If it is loaded in full **"Full Load"** is displayed.

- End time: The end time of the data in the Segment. If it is loaded in full **"Full Load"** is displayed.

- Index: Indexes in this segment / Total indexes

- Status: Segment status. You can find a detailed introduction [Segment Status](#status) section.

- Last Updated Time: Segment last updated time.

- Source Records: The source records of the data in the segment.

- Storage: The storage size of the data in the segment.

  > Tip: When the tiered storage is turned on, the storage size of the data loaded into the tiered storage (ClickHouse) will be displayed.

- Actions: The operation of the segment. Currently only **Show Detail** is supported.



### <span id="status">Segment Status</span>

You can view the segment status in the segment list. There are 6 types of segment statuses. See below:

- **ONLINE**: Segment can serve the query by indexes loaded data or pushdown engine.
- **WARNING**: The data in the segment has been loaded and can serve the query. However, the source data might be changed which might cause the data inconsistent. It's highly recommended to refresh all indexes within this segment.
- **LOCKED**: Segments that are refreshing or merging will be locked.
- **LOADING**: The data in the segment is loading.
- **REFRESHING**: A new segment is automatically generated when you refresh the specified segment. This new segment is marked as *REFRESHING*. When the refresh is complete the old segment will be automatically deleted.
- **MERGING**: A new segment is automatically generated when you merge the specified segments. This new segment is marked as *MERGING*. When the merge is complete the old segment will be automatically deleted.



### <span id="expert_operation">Segment Operation</span>

You have 6 types of segment operations on the **Data Asset -> model **page.

- **+ Segment**: Add segments to define the model’s data range for serving queries. Queries within the range could be answered by indexes or pushdown engine. Queries out of the range would have no results. The button is located above the segment list.

> **Note:** In **Setting -> Basic Settings -> Segment Settings**, enable **Creating Reserved Segments**, then the **+ Segment** operation button will appear.

- **Show Detail**: You can click the icon on the right side of the segment list. When your mouse is hovering over the icon **Show Detail** is displayed. You can view details such as storage size, the data range and more.

- **Refresh**: Refresh the data in the segment. This operation supports batch refresh. The **Refresh** button is located above the segment list.

  >**Note**: Only ONLINE and WARNING status segments can be refreshed.

- **Merge**：Merge multiple segments as a new one.  The **Merge** button is located above the segment list.

  > **Note**: Only ONLINE and WARNING status segments can be merged.

- **Delete**: Delete the segment. This operation supports batch deletion. The **Delete** button is located above the segment list.

- **Fix**: Fix the discontinuous segments. This button will be only displayed above the Segment list when the holes exists in Segment ranges.



### <span id="setting"> Segment Settings</span>

You can set some to manage segments automatically in the **Setting -> Segment Settings** page. Please refer to [Project Settings](../../operations/project-managing/project_settings.md) for the specific requirements.


In the incremental build mode, as the number of segments increases, the system may need to aggregate multiple segments to serve the query, which degrades the query performance and the query performance decreases. At the same time, a large number of small files will put pressure on the HDFS Namenode and affect the HDFS performance. Apache Kylin provides a mechanism to control the number of segments - **Segments Merge **.


### <span id="manual">Manual Merge</span>

You can merge multiple Segments in the Web GUI or using **Segment Manage API**.

In the web GUI
1. In the Data Assets -> Model -> Segment list, select the Segments to be merged.
2. Click "Merge" in the drop-down list, check that three conditions are met (consistent indexes, consistent sub-partition values, and continuous time ranges) , and submit the merge task.
   The system submits a task of type "Merge Data". Until the task is completed, the original segment is still available. After the task is completed, it will be replaced by a new segment. To save system resources, the original segments will be recycled and cleaned up.

### <span id="auto">Auto Merge</span>

Merging Segments is very simple, but requires manual triggering of the merge from time to time. When there are multiple projects and models in the production environment, it becomes very cumbersome to trigger the merge operation one by one. Therefore, Apache Kylin provides a segment automatic merging solution.
- [Auto-Merge settings](#setting)
- [Auto-merge strategy](#strategy)
- [Choose Segment](#choose)
- [Try Merge](#trymerge)
- [Notice](#notice)

#### <span id="setting">Auto-Merge settings</span>

According to different business needs, it supports the automatic merging of project and model settings respectively. If the two merge strategies are different, the system adopts the model-level settings.
- Project-level: Used for all models in a project, with the same merge strategy.
- Model-level: used for multiple models in a project, with different automatic merging strategies.

Please refer to **Segment Settings** and **Model/Index Group Rewrite Settings** of [Project Settings](../../operations/project-managing/project_settings.md) for the specific requirements.

#### <span id="strategy">Auto-merge strategy</span>
- Merge Timing: The system triggers an automatic merge attempt every time a new segment in the project becomes complete. To ensure query performance, all segments will not be merged at once.

- Time Threshold: Allows the user to set a time threshold of up to 6 layers. The larger the layer, the larger the time threshold. The user can select multiple levels (eg week, month).
  Note: day, week and month represent natural day, natural week and natural month respectively.

  
  
  
  | level | Time Threshold |
  | ----- | -------------- |
  | 1     | hour           |
  | 2     | day            |
  | 3     | week           |
  | 4     | month          |
  | 5     | quarter        |
  | 6     | year           |

#### <span id="choose">Choose Segment</span>

When triggering an Auto-Merge, the system attempts to start from maximum layer time threshold, skips segments whose time length is greater than or equal to the threshold, select remaining eligible Segments (consistent indexes, consistent sub-partition values, and continuous time ranges).

#### <span id="trymerge">Try Merge</span>
When the total time length of the segments reaches the time threshold, they will be merged. After the merge task is completed, the system will trigger an Auto- Merge attempt again; otherwise, the system repeats the search process using the time threshold for the next level. Stop trying until all the selected levels have no segment that meets the condition .

#### <span id="notice">Notice</span>
- The Auto-Merge of week is constrained by month, that is, if a natural week spans months/quarters/years, they are merged separately. (see example 2).
- During the process of merging segments, the HDFS storage space may exceed the threshold limit, causing the merging to fail.


### <span id="example">Example of Auto Merge</span>

- [Example 1](#ex1)
- [Example 2](#ex2)

#### <span id="ex1">Example 1</span>
The switch for Auto-Merge is turned on, and the specified time thresholds are week and month. There are six consecutive Segments A~F.



| Segment (Initial) | Time Range              | Time Length |
| ----------------- | ----------------------- | ----------- |
| A                 | 2022-01-01 ~ 2022-01-31 | 1 month     |
| B                 | 2022-02-01 ~ 2022-02-06 | 1 week      |
| C                 | 2022-02-07 ~ 2022-02-13 | 1 week      |
| D                 | 2022-02-14 ~ 2022-02-20 | 1 week      |
| E                 | 2022-02-21 ~2022-02-25  | 5 days      |
| F                 | 2022-02-26 Saturday     | 1 day       |

Segment G was added later (Sunday 2022-02-27).

- Now there are 7 segments A~G, the system first tries to merge by month, since Segment A's time length is greater than or equal to the threshold (1 month), it will be excluded. The following segments B-G add up to less than 1 month, do not meet the time threshold (1 month), and therefore cannot be merged by month.

- The system will try the next level of time thresholds (i.e. merged by week). The system rescans all segments, finds that A, B, C, and D are all greater than or equal to the threshold (1 week), so they are skipped. The following segments E-G add up to the threshold (1 week) and merge into Segment X.

- With the addition of segment X, the system will be triggered to restart the merge attempt, but the attempt will be terminated because the conditions for automatic merge have not been met.

  

| Segment(Add G,  Trigger Auto-Merge） | Time Range              | Time Length |
| ------------------------------------- | ----------------------- | ----------- |
| A                                     | 2022-01-01 ~ 2022-01-31 | 1 month      |
| B                                     | 2022-02-01 ~ 2022-02-06 | 1 week        |
| C                                     | 2022-02-07 ~ 2022-02-13 | 1 week        |
| D                                     | 2022-02-14 ~ 2022-02-20 | 1 week        |
| X（Orignal E-G)                      | 2022-02-21 ~ 2022-02-27 | 1 week        |

Add Segment H  ( 2022-02-28)

- Trigger the system to try to merge by month, all segments except A add up to the threshold (1 month), so B-H are merged into Segment Y.

- With the addition of Segment Y, the system will trigger the merge attempt again, but the conditions for Auto-Merge have not been met, and the attempt is terminated.

  

| Segment（Add H,  Trigger Auto-Merge） | Time Range              | Time Length |
| ------------------------------------- | ----------------------- | ----------- |
| A                                     | 2022-01-01 ~ 2022-01-31 | 1 week      |
| Y （Orignal B-H）                     | 2022-02-01 ~ 2022-02-28 | 1 week      |

#### <span id="ex2">Example 2</span>
There are six consecutive segments A~F, and their own time lengt are all 1 day. At this time, turn on the "auto merge" switch, specify the time threshold as weeks.



| Segment (Initial) | Time Range           |
| ----------------- | -------------------- |
| A                 | Monday 2021-12-27    |
| B                 | Tuesday 2021-12-28   |
| C                 | Wednesday 2021-12-29 |
| D                 | Thursday 2021-12-30  |
| E                 | Friday 2021-12-31    |
| FS                | Saturday 2022-01-01  |



Then Segment G was added (Sunday 2022-01-02) with a duration of 1 day.

- At this point there are 7 consecutive Segments, forming a natural week spanning 2 years. The system tries to merge by week, A-E is merged into a new Segment X.

  

| Segment（Add G,  Trigger 1st Auto-Merge） | Time Range                                 |
| ----------------------------------------- | ------------------------------------------ |
| X（Orignal A-E）                          | Monday to Friday (2021-12-27 ~ 2021-12-31) |
| F                                         | Saturday 2022-01-01                        |
| G                                         | Sunday 2022-01-02                          |

- With the addition of Segment X, the system will be triggered to merge by week, so F-G will be merged into a new Segment Y.

  

| Segment（Add X,  Trigger 2nd Auto-Merge） | Time Range                                   |
| ----------------------------------------- | -------------------------------------------- |
| X（Orignal A-E）                          | Monday to Friday (2021-01-27 ~ 2021-01-31)   |
| Y（Orignal F-G）                          | Saturday to Sunday (2022-02-01 ~ 2022-02-02) |

- With the addition of Segment Y, the attempt to merge the system by week is triggered again. Now there are no segments with a duration of 1 week (in each year), so the attempt stops.
