---
title: Recommendation
language: en
sidebar_label: Recommendation
pagination_label: Recommendation
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - recommendation
draft: false
last_update:
    date: 09/02/2024
---

Following the manual modeling section, it’s evident that the work involved can be tedious and labor-intensive, relying heavily on the expertise of data modelers. In this chapter, we will introduce an easier way to accomplish this work in Kylin 5.0 through a feature called **Recommendation**. Note that this type of recommendation differs from the its counterpart in machine learning, but it significantly improves your efficiency. We will cover the following topics:

- **[Imported SQL Modeling](sql_modeling.md)**: The Kylin recommendation engine can recognize input SQL statements, extract dimensions, measures, and other critical information, and automatically translate this data into models and indexes. This means you do not need in-depth knowledge to create models that meet your needs, allowing you to focus more on your business.

- **[Query History Recommendation](optimize_by_qh.md)**: The Kylin recommendation engine periodically analyzes query history records and transforms them into recommendations. You just need to review the proposed recommendations and make any necessary adjustments to the configuration to achieve your desired outcomes. Once you approve them, query performance can be significantly accelerated.

- **[Index Optimization](index_optimization.md)**: The Kylin recommendation engine analyzes the relationships among existing indexes to identify those that are useless or ineffective. It then generates recommendations for deletion, helping you save time on building and managing indexes as well as conserving storage space.

Before the journey, you should turn on the recommendation function in the project setting page.

![turn_on_recommendation.gif](images/turn_on_recommendation.gif)

Now, let’s embark on this journey.



