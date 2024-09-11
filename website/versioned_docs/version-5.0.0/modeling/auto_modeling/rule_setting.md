---
title: Set Recommendation Rules
language: en
sidebar_label: Set Recommendation Rules
pagination_label: Set Recommendation Rules
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - recommendation
    - rule
draft: false
last_update:
    date: 09/02/2024
---

Kylin optimizes query performance with index or model recommendations based on query history, data characteristics, SQL modeling, etc. The [Recommendation](intro.md) feature can simplify model building and accelerate queries. This article covers how to configure the setting for the recommendations. 

### **Prerequisites**

Please make sure that the **Recommendation** feature is turned on.  

:::info

 To turn on the feature, go to the **Home** page of Kylin and click **Turn on Recommendation**. 
:::

### **Background information**

With the "Recommendation" feature enabled, recommendations will be automatically generated and temporarily recorded in the internal system. After you set up the generation rules, the system will only prompt the recommendations that meet the criteria. You can check the recommendations on the **Home** page. 

For more information about recommendation, see [Basic Concepts and Principles](basic_concept_actions.md). 

### **Operation steps**

1. Log in to Kylin. 

   - System admin
   - **Management** or **Admin** of the target project 

2. In the left navigation panel, click **Setting**.

3. On the **Basic Settings** tab, go to the **Recommendation Preferences** section.

4. Specify the following parameters for the rule. Then click **Save** to finish the rule setting. 

   ![](images/recommendation_preferences_en.png)

   - **User Rules:** Turn on the toggle switch if you only want to generate recommendations for selected users and/or user groups. Then you can select users and/or user groups in the following field(s). 

   - **Duration Rule:** Generate recommendations for queries within certain duration. Then you can customize the query duration. Default: 5 to 3600 second(s).


   - **Hit Rules**: Set the parameters below to filter out indexes that are frequently hit by queries, that is, the indexes of high value. 
     For example, if the **Time Frame** is set to 2 and **Number of Hits** is set to 30, the system will only prompt recommendations with at least 30 query hits in the past 2 days.
        - **Time Frame**: Set the number of days to be calculated for the query hits, starting from today, default: 2 days.
        - **Number of Hits**: Set the number of query hits (default: 30 hits). This hit count can later be modified based on the recommendations. 
     
   - **Limit of Recommendations for Adding Index**: Set how many recommendations will be prompted and the push frequency.  

     :::info
     
      This parameter is only valid for recommendations based on query history. 
        - Number of recommendations: Set the number of recommendations that will be pushed. Recommendations will be ranked based on the query hit rates (from highest to lowest); the default value is 20, indicating only the top 20 recommendations will be prompted. 
        - **Recommendation Frequency**: Set the frequency (in days) to push the recommendations, default: 2 days.
     :::
5. On the **Basic Settings** tab, go to the **Exclude Column Rule Setting** section to specify exclude columns.

   If turn on this switch, columns can be added after searching the specified table through keywords, and the system will not generate optimization suggestions for exclude columns.

   * Exclude columns is not suitable for fact table. In fact, if a table having exclude columns is chosen as fact table in a model, the excluding rules actually will do nothing for these columns.
   * Exclude columns are used to handle slowly changing dimensions, especially when you want to use SCD type 1 for some columns and SCD type 2 for others. 
   * If you enable intelligent recommendation, the exclude columns will not be recommended to the optimization suggestions. This function applies to the scenario of AS-IS (SCD type 1) analysis.
   * After the Settings take effect, the foreign keys in the fact table are recommended for optimization recommendations instead of the exclude columns, and the columns in the query table can be derived answer from the foreign keys after the index is built.
   * If exclude columns are already used in the indexes, you need to manually delete these indexes dependent on these columns to implement AS-IS (SCD type 1).
   * If you want to implement AS-WAS (SCD type 2) on other columns of lookup table, these columns can be added into index

:::info

 * By default，the 'Exclude Table' function is replaced by 'Exclude Column'，you can set `kylin.metadata.table-exclusion-enabled=true` to enable it in project settings.
 * You can exclude all columns on a table to get the effect of exclude table.
 * The former parameter of exclude table: `kylin.engine.build-excluded-table=true` is now abandon.
:::

:::info Exclude Column
 * For join relation, in snowflake model, A join B(on a1 = b1) and B join C (on b2 = c2), the foreign key b2 cannot be a exclude column, otherwise the join chain will be broken when proposing recommendations.
 * The design of computed columns indicates that precomputation is required, while the design of exclude columns indicates that precomputation is not required. So these two are incompatible with each other. Therefore, it is not recommended to define the computed columns contains exclude columns in principle. In particular, computed columns and measures that dependent on exclude columns  may result in incorrect data if they hit the index. 
 * Computed columns dependent on excluded columns are not reused for the intelligent recommendation. Moreover, with the setting of `kylin.metadata.only-reuse-user-defined-computed column=true`(by default false), only the customized computed columns will be used for recommendations, that is to say, no more computed columns started with `CC_AUTO` will be created in the model.
 * For queries, by default indexes answer the query with a higher priority than using the foreign key and snapshot. After the exclude column is enabled and setting `kylin.query.snapshot-preferred-for-table-exclusion=true`, the system preferentially selects the foreign key index to runtime join snapshot to response the query. When the '**Exclude Column Rule Setting**' is enabled, this is the default behavior. However, by setting it to false, index independent on exclude columns will be preferred to answer. For example, the model is A join B (on a1 = b1), the index1 is `{A.a1, A.a3}`, the index2 is `{A.a3, B.b1}`, and the query is `select A.a3, B.b1 from A join B on a1 = b1`, if the  '**Exclude Column Rule Setting**' is enabled, this query will hit index1 to runtime join snapshot of B, but if `kylin.query.snapshot-preferred-for-table-exclusion=false` this query will hit index2.

:::

### **FAQ**

- Question: At which level will the **Recommendations Preferences** be effective? 

  Answer: At the project level. To exclude certain tables at the model level, that is, this table will not be calculated during the pre-computation and indexing, please deselect the **Precompute Join Relationships** checkbox in the **Add Join Relationship** dialog box. 

