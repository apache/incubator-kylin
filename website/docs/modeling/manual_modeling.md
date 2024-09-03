---
title: Manual Modeling
language: en
sidebar_label: Manual Modeling
pagination_label: Manual Modeling
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - manual modeling
draft: false
last_update:
    date: 08/19/2022
---

Kylin follows multidimensional modeling theory when building star or snowflake models based on your tables. Kylin also leverages pre-computation technique and will reuse the pre-computed results to answer queries, so there is no need to traverse all data when there is a query, and thus achieve sub-second query times on PB-level data.

### **Operation steps** 

Kylin model consists of multiple tables and their join relations. In this article, we use this [SSB dataset](../quickstart/sample_dataset.md), a dataset based on real business applications, and hope to analyze products and supplier information from dimensions such as year, city, supplier name and brand. To achieve this goal, we will create a model in Kylin, and then set dimensions and measures for multidimensional analysis. See the table below for detailed steps. 

| **Step**                                                     | **Description**                                              |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| Step 1: Create a model and add a fact table and dimension tables | In this step, we design the model, and then define the fact table and dimension tables to be analyzed, which will serve as data sources for later data analysis. |
| Step 2: Create join relations among tables                   | Create join relations between the foreign keys of the fact table and the primary keys of the dimension tables to achieve join queries of the two tables. |
| Step 3: Add dimensions and measures to the model             | Set the dimensions and measures for data analysis. Kylin will run pre-computation based on the combination of the defined dimensions and measures, which will greatly accelerate data query. |
| Step 4: Save the model and set the loading method            | Save the model settings and specify the data loading method for the pre-computations step. If incremental load is selected, data within a specified time range will be loaded to improve loading efficiency. |

### **Step 1: Create a model and add a fact table and dimension tables** 

1. Log in to Kylin as any of the following roles:

   - System admin 
   - **Management** or **Admin** of the target project

2. To create a model: 

   1. In the left navigation panel, click **Data Asset** > **Model**.

   2. Click **+ Model**. 

   3. In the pop-up dialog box, enter a name and description for the model, and then click **Submit**.

      Model name can be any combination of numbers, letters, and underscores (_). 

3. You will be directed to the model editing page. On this page, add the fact table to the model. 

   Fact table is used to store fact records, that is, to store data about a business process at the finest granularity, for example, product sales table. It often serves as the primary table of a model.

   1. In the left-hand **Data Source** section, find the target fact table (**P_LINEORDER** in this example).  

      > [!NOTE]
      >
      > If there is no table in the **Data Source** section, please [load data source](../datasource/intro.md) first.

   2. Drag the target table to the right-hand canvas and select **Switch to Fact Table**.

      ![](images/switch_to_fact_table.png)

4. Add dimension tables to the model. 

   Dimension table, also called lookup table, is used to store repeated attributes of the fact table, such as date and geographic location. Dimension tables can help to reduce the fact table size and improve dimension management efficiency. 

   1. In the left-hand **Data Source** section, find the target dimension table. 

   2. Drag the target table to the right-hand canvas. 

      To add multiple dimension tables, repeat this step for each table. As shown below, one fact table and 4 dimension tables are added.

      ![](images/add_tables.png)

### **Step 2: Create join relations among tables** 

1. On the model editing page, drag a column to create a join relation between the foreign key of the fact table and the primary key of the dimension table. 

   ![](images/create_join_relations.gif)

2. In the **Add Join Relationship** dialog box, follow the instructions below to set the join relation. 

   ![](images/add_join_relations.png)

   - **Join Relationship for Tables**: It includes 3 drop-down lists. The first and the third one specify the tables to be joined, and the second one defines the join relation. Kylin currently supports **LEFT** (left join) and **INNER** (inner join). 

   - **Table Relationship:** Select the mapping between the foreign and primary keys: **One-to-One or Many-to-One**, or **One-to-Many or Many-to-Many**.  

   - **Precompute Join Relationship**: Select whether to expand the joined tables into a flat table based on the mappings. This option is selected by default. For more information about this function and its applicable scenarios, see [Precompute the join relations](model_design/precompute_join_relations.md). 

   - **Join Relationship for Columns**: It includes 3 drop-down lists. The first and the third one specify the columns to be joined, and the second one defines the join relation, which is equal-join (=) by default. Join relations should meet the following requirements:  
     - Do not define more than one join relation for the same column; two tables could only be joined by the same condition for one time
     - Join relations for columns should include at least one equal-join condition (=)
     - Join relations ≥ and < must be used in pairs, and the column in between must be the same. Example: B ≥ A, C < A

3. Click **OK**.

To create join relations for multiple tables, repeat steps 1-3 for each table. In this example, we create 4 join relations for tables, which constitute a star model. 

![](images/star_model_created.png)

Corresponding SQL statements:

```SQL
P_LINEORDER LEFT JOIN DATES ON P_LINEORDER.LO_ORDERDATE = DATES.D_DATEKEY
P_LINEORDER LEFT JOIN CUSTOMER ON P_LINEORDER.LO_CUSTKEY = CUSTOMER.C_CUSTKEY
P_LINEORDER LEFT JOIN SUPPLIER ON P_LINEORDER.LO_SUPPKEY = SUPPLIER.S_SUPPKEY
P_LINEORDER LEFT JOIN PART ON P_LINEORDER.LO_PARTKEY = PART.P_PARTKEY
```

### **Step 3: Add dimensions and measures to the model**

1. To add dimension tables to the model: 

   1. On the model editing page, drag dimension columns from dimension tables to the **Dimension** section. 

       To add dimensions in batch, click **+** in the **Dimension** section.

       ![](images/add_dimention.gif)

   2. In the pop-up dialog box, set the dimension name.

       By default, it's the column name. It can be any combination of letters, numbers, spaces, and special characters `(_ -()%?)`. 

   3. Click **OK**. 

       In our example, we added year (D_YEAR in DATE), the city customer is in (CITY in CUSTOMER), supplier name (S_NAME in SUPPLIER), and brand (P_BRAND in PART) as dimensions.    

2. Add measures to the model.

   1. On the model editing page, drag dimension columns from dimension tables to the **Measure** section. 

       To add dimensions in batch, click **+** in the **Measure** section.

      ![](images/add_measure.gif)

   2. In the **Add Measure** dialog box, follow the instructions below to complete join relation settings.

      - **Name**: Column name by default. It can be any combination of letters, numbers, spaces, and special characters `(_ -()%?)`. 

      - **Function**: **SUM (column)** by default. Kylin has a variety of built-in basic and advanced functions, such as Count Distinct, TopN, etc. For more information, see [Advanced measures](model_design/advance_guide/intro.md).  

      - **Column**: The measure column. No adjustment is needed. 

      - **Note** (Optional): Enter notes to facilitate future measure management.

   3. Click **Submit**. 

      In our example, we added revenue (LO_REVENUE in P_LINEORDER) and supply cost (LO_SUPPLYCOST in P_LINEORDER) as measures, and wanted to calculate the sum for each.  

3. (Optional) To achieve complex processing and computation based on the existing columns, you can add computed columns to the model. For more information, see [Computed columns](model_design/computed_column.md).

### Step 4: Save the model and set the loading method

1. In the bottom right corner of the model editing page, click **Save.**

2. In the **Save** dialog box, follow the instructions below to complete model settings.

   ![](images/save_load_method.png)

   - **Please select a load method**:
     - **Full Load**: Load and pre-compute all data in the source table according to different combinations of dimensions and measures.
     - **Incremental Load**: Load and pre-compute data within the specified time range in the source table according to combinations of dimensions and measures. You also need to specify the following parameters if this option selected. 
       - **Partition Table**: Fact table (default and cannot be changed) 
       - **Time Partition Column**: Select a column of the time type in the partition table. 
       - **Time Format**: Select the time format. Or you can click ![](images/time_format.png) and Kyligece will automatically fill in the time format.
   - **Advanced Setting**: Use the data filter to filter out null values or data meeting certain requirements. Use `AND` or `OR` to associate multiple filters, for example, `BUYER_ID <> 0001 AND COUNT_ITEM > 1000 OR TOTAL_PRICE = 1000`. 
   - **Add Base Indexes**: Add the following base indexes. This option is enabled by default so base indexes will automatically update when model dimensions and measures change. 
     - Base aggregate index: It includes all model dimensions and measures. 
     - Base table index: It includes all columns of model dimensions and measures.

3. Click **Submit**. 

   After the model is saved, you can click **View Index** in the **Notice** dialog box to check the aggregate index and table index that Kylin automatically creates. 

### Next steps

For the newly created base indexes, you need to [build them](load_data/build_index.md) so Kylin can run pre-computation based on these indexes to accelerate queries. 

> [!NOTE]
>
> There are few scenarios where base indexes can be used to accelerate queries. To improve query efficiency, you need to add more indexes to the model. For more information, see [Aggregate index](model_design/aggregation_group.md) and [Table index](model_design/table_index.md).

Model design refers to build the star model or snowflake model based on data table and multidimensional modeling theory. The main contents of model design are as follows:

- Define Fact Table and Dimension Table
- Define the Association Relationship between Tables
- Define Dimension and Measurement

### <span id="model">Model List</span>

You can create and design models manually. Below are the main content of model list:

1. Log in to Web UI, switch to a project.

2. Navigate to **Data Asset -> Model** page, where models are shown in a list. The picture below is the index group list:

   ![Model List](images/model_list.png)

   **Fields Explanation:**

    - **Model Name**: Model's name.

        - **Status**: There are four statuses.
        - *ONLINE* indicates this model is online and is able to answer queries.
            - *OFFLINE* indicates this model is offline and not available to answer queries. We recommend using offline when you need to edit the model.
            - *BROKEN* indicates this model is broken and not available. Mostly happens when the schemas of related source tables have changed, for instance, a related source table is deleted.
            - *WARNING* indicates this model is warning and can only server parts of queries. Mostly happens when the segments exist holes or indexes are waiting to build.
        - **Last Updated Time**: The lastest time to update model.

    - **More Actions**: The **More Actions** button will appear when you are hovering on model name area, please refer to [Model Operations](#operation) for details.

    - **Owner**: The user who created this model.

    - **Description**: Model description.

    - **Fact Table**: The fact table of this model.

    - **Types**: Model types, which include *Batch Model*

    - **Usage**: Hit count by SQL statements in the last 30 days. Update every 30 minutes.

    - **Rows**:  The rows of loaded data in this model.

    - **Storage**: The storage size of loaded data in this model, which combines the storage size of all Segments data.

      > Tip: When the tiered storage is turned on, the total storage size of the data loaded into the tiered storage (ClickHouse) will be displayed.

    - **Expansion Rate**: The ratio of the storage size of the built data to the storage size of the corresponding source table data under the model. Expansion Rate = Storage Size / Source Table Size.


	> Notice: The expansion rate won't show if the storage size is less than 1GB.
	
	- **Index Amount**: The amount of indexes in this model.

### <span id="operation">Model Operation</span>

You are only allowed to operate on models. You can hover on the right most column **Actions** of the model list to get the action names. Specific actions are listed below:

- **Edit**: Click on the pencil shape button, enter into the model editing page.

- **Build Index**: Loads data for models. You can choose the data range in the pop-up window.

- **Model Partition**: Set partition column for the model.

- **Export Model**: Export single model metadata.

  > **Note**: Since the locked indexes will be deleted after the new indexes have been built, the exported model metadata will not include the locked index.

- **Export TDS**: Export TDS file of the model .

- **Rename**: Renames the model.

- **Clone**: Clones an identical model. You can give a new name for this new model. The new model has the same fact table, dimension tables, join relationship, dimensions, measures, computed columns, date partition column, aggregate indexes, table indexes, etc. as the origin model. But the new model does not have data, you need to load data for this cloned model manually.

  > **Note**: Since the locked indexes will be deleted after the new indexes have been built, the cloned model will not include the locked index.

- **Change Owner**：Change model owner. Only system administrators and project administrators have the authority to modify model owner.

- **Delete**: Deletes the model, remove the loaded data at the same time.

- **Purge**: Purges all loaded data in this model.

- **Offline**: Makes a *Online / Warning* model offline. An offline model cannot answer any queries.

- **Online**: Makes a *Offline* model online. An online model should be able to answer related queries.

> **Note:** If the model is in *BROKEN* status, only the **delete** operation is allowed.


### <span id="more">Model Details</span>

Models contain Segments and indexes. You can click model name to unfold the detailed information, as shown below:

![Details](images/modellist_more_info.png)

- **Overview**: Check Overview details, please refer to [Model Overview](#overview) for more.
- **Data Features**: Check data features.
- **Segment**: Check Segment details, please refer to [Segment Operation and Settings](load_data/segment_operation_settings/intro.md) for more.
- **Index**: Review the model indexes.
    - **Index Overview**: Check index overview.
    - **Aggregate Group**: Add or check defined aggregate indexes, please refer to [Aggregate Index](model_design/aggregation_group.md) for more details.
    - **Table Index**: Add or check defined table indexes, please refer to [Table Index](model_design/table_index.md) for more details.
- **Developers**: Check information for developers.
    - **JSON**: Kylin describes the information of models (index groups) in `JSON` format, such as design, dimensions, measures, etc.
    - **SQL**: The SQL statement consists of related information about tables and columns in the model, such as the join conditions between the tables.

### <span id="overview">Model Overview</span>

After expanding the model information, you can see the model overview page, which will help you to quickly get the model information.

![Model Overview](images/model_overview/unfold_model.png)

On this page, you can view the ER diagram of the model.

![View ER Diagram](images/model_overview/er.png)

What's more, you can view the dimensions and measures information contained in the model.

![View Dimensions Information](images/model_overview/dimensions.png)

![View Measures Information](images/model_overview/measures.png)


### FAQ

- Question: Why did I get an error when saving the time partition column settings?

  Answer: This error occurs when the time format of the time partition column does not match the target format. Kylin supports the following time formats:  `yyyyMMdd`, `yyyy-MM-dd`, `yyyy/MM/dd`, `yyyy-MM-dd HH:mm:ss`, `yyyy-MM-dd HH:mm:ss.SSS`, `yyyy-MM`, `yyyyMM` and `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`.

  Kylin also supports customized time formats if the following conditions are met:

  - Any combination of yyyy, MM, dd, HH, mm, ss, and SSS is used with the elements in ascending order. 
  - Hyphens (-), forward slashes (/), columns (:), or spaces are used as separators. 
  - Unformatted letters are enclosed in single quotation marks ('). For example, 'T' is recognized as T.

  > [!NOTE] 
  >
  > When the time format is customized as `yyyyMMddHHmmss`, the corresponding column in the Hive table should be strings, or Kylin may fail to recognize column data.
  
- Question: I've modified several tables from the same model. Why did I get an error when reloading these tables? 

  Answer: Since Kylin currently only supports loading a single table at a time, please edit and reload tables one by one, rather than reload several modified tables at a time.  

- Question: I've modified several tables from the same model. Why did I get an error when reloading these tables? 

  Answer: Since Kylin currently only supports loading a single table at a time, please edit and reload tables one by one, rather than reload several modified tables at a time.  
  
- Question: What are the rules for the model to go online?

  Answer: It will automatically switch to online when the building job completed. However, customer may need to continually build some historical data during a period when creating a new report or testing, and may not want this model to serve any query unless it has built all data. In this case, we also offer a model level configuration to ensure users can control the model status manually . After configuring the `kylin.model.offline` to `true` (default value is false) in model rewriting page, the model will not switch to online status even if the building job completed. Therefore, user will not worry about the model disturb the query route.
  
  ![](images/add_model_set.png)
