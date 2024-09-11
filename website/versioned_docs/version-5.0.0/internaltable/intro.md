---
title: Internal Table Overview
language: en
sidebar_label: Internal Table Overview
pagination_label: Internal Table Overview
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - internal table
    - native engine
draft: false
last_update:
    date: 08/05/2024
---

This chapter introduces new feature of Kylin, what is internal table and how it works.

### Flexible Analysis, Jumping Sparks of Thought

Kylin's pre-calculated technology can help you achieve a sub-second query experience in scenarios with large amounts of data and high concurrency by defining models and indexes. However, not all query and analysis scenarios can be predefined, and from the perspective of construction and storage costs, it is not suitable to pre-calculate indexes for all combinations of dimensions.

Before the business analysis model is fixed, analysts often need to explore flexibly to mine the value and patterns of data, or to prepare for making fixed reports. Due to the variability of analysis dimensions and measures, a limited number of pre-calculated indexes cannot fully cover all query scenarios, and at this time, the on-the-fly calculation capability based on the internal table can come into play.

### What is an Internal Table?

An internal table refers to a table that is directly managed by Kylin in terms of data and metadata.
Compared with the original data source table that only imports table metadata for modeling, the internal table not only saves metadata but also directly manages user data. Like traditional RDBMS and most data warehouse software, you only need to import data into the internal table to perform query analysis.

### How to Create an Internal Table?

You don't have to write DDL statements by yourself. Just click the "Create Internal Table" button on the page of the imported data source table and complete the relevant table property settings to complete the creation of the internal table.
For details, please refer to the [Manage Internal Tables](./internal_table_management.md) page.

### How to Distinguish Between Data Source Tables and Internal Tables?

There is no need to distinguish between data source tables and internal tables during the query. You can consider them as one table in Kylin, which only has a database name and a table name. The difference lies in that when the internal table function is not turned on or the internal table is not created, the table does not directly manage data and is only used for modeling.

### How to Enable the Use of Internal Table Features at the Project Level?

You only need to go to the project settings page, click on the "Internal Table Settings" tab, and turn on the switch for the internal table feature.

After turning on the internal table switch, you can see the "Internal Table" tab in the side navigation bar. Click on it to enter the internal table management page.

### About the Snapshot Feature

In Kylin 4.x and earlier, the system supports the creation of snapshot tables. Starting from Kylin 5.x, when the internal table function is turned on, the snapshot feature will no longer be supported.
