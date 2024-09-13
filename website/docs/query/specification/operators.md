---
title: Operators
language: en
sidebar_label: Operators
pagination_label: Operators
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - operators
draft: false
last_update:
    date: 08/17/2022
---

This chapter will introduce operators that applicable to SQL statements.

### Arithmetic Operators

| Operator | Description       | Syntax | Example               |
|----------|-------------------|--------|-----------------------|
| +        | Plus operator     | A + B  | Cost + Profit         |
| -        | Minus operator    | A - B  | Revenue - Cost        |
| *        | Multiply operator | A * B  | Unit_Price * Quantity |
| /        | Divide operator   | A / B  | Total_Sale / Quantity |

### Comparison Operators

| Operator             | Description                                                                                            | Syntax                                      | Example                                        |
|----------------------|--------------------------------------------------------------------------------------------------------|---------------------------------------------|------------------------------------------------|
| \<                   | Less than                                                                                              | A \< B                                      | col1 \< col2                                   |
| \<=                  | Less than or equal                                                                                     | A \<= B                                     | col1 \<= col2                                  |
| \>                   | Greater than                                                                                           | A \>= B                                     | col1 \> col2                                   |
| \>=                  | Greater than or equal                                                                                  | A \>= B                                     | col1 \>= col2                                  |
| \<>                  | Not Equal                                                                                              | A \<> B                                     | col1 \<> col2                                  |
| IS NULL              | Whether *value* is null                                                                                | value IS NULL                               | col1 IS NULL                                   |
| IS NOT NULL          | Whether *value* is not null                                                                            | value IS NOT NULL                           | col1 IS NOT NULL                               |
| IS DISTINCT FROM     | Whether two values are not equal, treating null values as the same                                     | value1 IS DISTINCT FROM value2              | col1 IS DISTINCT FROM col2                     |
| IS NOT DISTINCT FROM | Whether two values are equal, treating null values as the same                                         | value1 IS NOT DISTINCT FROM value2          | col1 IS NOT DISTINCT FROM col2                 |
| BETWEEN              | Return true if the specified value is greater than or equal to value1 and less than or equal to value2 | A BETWEEN   value1 AND value2               | col1 BETWEEN '2016-01-01' AND '2016-12-30'     |
| NOT BETWEEN          | Whether *value1* is less than *value2* or greater than *value3*                                        | value1 NOT BETWEEN value2 AND value3        | col1 NOT BETWEEN '2016-01-01' AND '2016-12-30' |
| LIKE                 | Whether *string1* matches pattern *string2*, *string1* and *string2* are string types                  | string1 LIKE string2                        | col1 LIKE '%frank%'                            |
| NOT LIKE             | Whether *string1* does not match pattern *string2*, *string1* and *string2* are string types           | string1 NOT LIKE string2 [ ESCAPE string3 ] | col1 NOT LIKE '%frank%'                        |
| SIMILAR TO           | Whether *string1* matches *string2* in regular expression                                              | string1 SIMILAR TO string2                  | col1 SIMILAR TO 'frank'                        |
| NOT SIMILAR TO       | Whether *string1* does not match *string2* in regular expression                                       | string1 NOT SIMILAR TO string2              | col1 NOT SIMILAR TO 'frank'                    |


Limitations
- The current SIMILAR TO ESCAPE syntax is limited to scenarios that support adding and hitting the model in SQL statements, and other scenarios such as adding computable columns.
- The string literals including specific symbols need to be escaped by default and the escape character is `\`. For example, to match `\kylin` , it should be using `\\kylin`. For `SIMILAR TO` and  `NOT SIMILAR TO` function, the functions use regex match and there is an escaped process. For example, for `\\\\kylin`, the result will be `true` when using  `SIMILAR TO` to compare with `\kylin` and `\\kylin`.

### Logical Operators

This section introduces the logical operators supported by Apache Kylin. The values of logical propositions are TRUE, FALSE, and UNKNOWN. The following `boolean` refers to a logical proposition.

| Operator     | Description                                                            | Syntax                | Example                       |
|--------------|------------------------------------------------------------------------|-----------------------|-------------------------------|
| AND          | Whether *boolean1* and *boolean2* are both TRUE                        | boolean1 AND boolean2 | Name ='frank' AND gender='M'  |
| OR           | Whether *boolean1* is TRUE or *boolean2* is TRUE                       | boolean1 OR boolean2  | Name='frank' OR Name='Hentry' |
| NOT          | Whether *boolean* is not TRUE; returns UNKNOWN if *boolean* is UNKNOWN | NOT boolean           | NOT (NAME ='frank')           |
| IS FALSE     | Whether *boolean* is FALSE; returns FALSE if *boolean* is UNKNOWN      | boolean IS FALSE      | Name ='frank' IS FALSE        |
| IS NOT FALSE | Whether *boolean* is not FALSE; returns TRUE if *boolean* is UNKNOWN   | boolean IS NOT FALSE  | Name ='frank' IS NOT FALSE    |
| IS TRUE      | Whether *boolean* is TRUE; returns FALSE if *boolean* is UNKNOWN       | boolean IS TRUE       | Name ='frank' IS TRUE         |
| IS NOT TRUE  | Whether *boolean* is not TRUE; returns TRUE if *boolean* is UNKNOWN    | boolean IS NOT TRUE   | Name ='frank' IS NOT TRUE     |

### String Operators
| Operator | Description                                 | Syntax   | Example                   |
|----------|---------------------------------------------|----------|---------------------------|
| \|\|     | Concatenates two strings or string columns  | A \|\| B | First_name \|\| Last_name |
