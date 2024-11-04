---
layout: docs31
title:  Cube Build and Job Monitoring
categories: tutorial
permalink: /docs31/tutorial/cube_build_job.html
---

### Cube Build
First of all, make sure that you have authority of the cube you want to build.

1. In `Models` page, click the `Action` drop down button in the right of a cube column and select operation `Build`.

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/action-build.png)

2. There is a pop-up window after the selection, click `Start Date` and `End date` input box to select date/time range of this incremental cube build.

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/date.png)

4. Click `Submit`, you will see the new job in the `Monitor` page.

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/jobs-page.png)

5. The new job is in "pending" status; after a while, it will be started to run and you will see the progress by refresh the web page or click the refresh button.

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/job-progress.png)


6. Wait the job to finish. In the between if you want to discard it, click `Actions` -> `Discard` button. If the job is failed, you can click `Resume` to retry.

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/discard.png)

7. After the job is 100% finished, the cube's status becomes to "Ready", means it is ready to serve SQL queries. In the `Model` tab, find the cube, click cube name to expand the section, in the "Storage" tab, it will list the cube segments. Each segment has a start/end time; Its underlying HBase table information is also listed.

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/cube-segment.png)

If you have more source data, repeat the steps above to build them into the cube.

### Job Monitoring
In the `Monitor` page, click the job detail button to see detail information show in the right side.

![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/job-steps.png)

The detail information of a job provides a step-by-step record to trace a job. You can hover a step status icon to see the basic status and information.

![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/hover-step.png)

Click the icon buttons showing in each step to see the details: `Parameters`, `Log`, `MRJob`.

* Parameters

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/parameters.png)

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/parameters-d.png)

* Log
        
   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/log.png)

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/log-d.png)

* MRJob(MapReduce Job)

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/mrjob.png)

   ![](../../images/tutorial/1.5/Kylin-Cube-Build-and-Job-Monitoring-Tutorial/mrjob-d.png)


