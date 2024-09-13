---
title: Data Types
language: en
sidebar_label: Data Types
pagination_label: Data Types
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - data types
draft: false
last_update:
    date: 08/17/2022
---

Kylin supports a wide range of data types to meet various use case requirements. In this chapter, we provide an overview of the data types supported by Kylin, covering different categories such as numeric, string, date, and more specialized data formats. These data types are essential for ensuring compatibility and efficiency in query execution and data storage.

### Supported Data Types

| Data Types  | Description                                                                                                                                                                | Range of Numbers                                                                                         |
|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| tinyint     | Small integer numbers.                                                                                                                                                     | (-128，127)                                                                                               |
| smallint    | Small integer numbers.                                                                                                                                                     | (-32,768，32,767)                                                                                         |
| int/integer | Integer numbers.                                                                                                                                                           | (-2,147,483,648，2,147,483,647)                                                                           |
| bigint      | Large integer numbers.                                                                                                                                                     | (-9,223,372,036,854,775,808，9,223,372,036,854,775,807)                                                   |
| float       | Single-precision floating point numbers.                                                                                                                                   | (-3.402823466E+38，-1.175494351E-38)，0，(1.175494351E-38，3.402823466351E+38)                               |
| double      | Double-precision floating point numbers.                                                                                                                                   | (-1.7976931348623157E+308，-2.2250738585072014E-308)，0，(2.2250738585072014E-308，1.797693134 8623157E+308) |
| decimal     | An exact numeric data type defined by its *precision* (total number of digits) and *scale* (number of digits to the right of the decimal point).                           | ---                                                                                                      |
| timestamp   | Values comprising values of fields year, month, day, hour, minute, and second, with the session local time-zone. The timestamp value represents an absolute point in time. | ---                                                                                                      |
| date        | Values comprising values of fields year, month and day, without a time-zone                                                                                                | ---                                                                                                      |
| varchar     | Variable length string                                                                                                                                                     | ---                                                                                                      |
| char        | Fixed length string                                                                                                                                                        | ---                                                                                                      |
| boolean     | Boolean values                                                                                                                                                             | ---                                                                                                      |

:::warning Note
When working with double type data, there is a potential issue with precision accuracy. This is due to the way floating-point numbers are represented in computer systems, which can lead to rounding errors or small inaccuracies in calculations, especially when dealing with very large or very small values. It's important to be mindful of this limitation when performing operations that require high precision, and consider alternative data types such as `decimal` for cases where exact accuracy is critical.
:::



### Example of Date

Three methods are available for querying date types. The following examples demonstrate how to query the `LO_ORDERDATE` field in the `SSB.P_LINEORDER` table.

**Method 1: Date Literal**

```sql
SELECT
    LO_LINENUMBER,
    LO_ORDERDATE,
    LO_ORDTOTALPRICE
FROM
    SSB.P_LINEORDER
WHERE
    LO_ORDERDATE = DATE '1992-06-03';
```

**Method 2: Explicit Date Cast**

```sql
SELECT
    LO_LINENUMBER,
    LO_ORDERDATE,
    LO_ORDTOTALPRICE
FROM
    SSB.P_LINEORDER
WHERE
    LO_ORDERDATE = CAST('1992-06-03' AS DATE);
```

**Method 3: Implicit Date Cast**

```sql
SELECT
    LO_LINENUMBER,
    LO_ORDERDATE,
    LO_ORDTOTALPRICE
FROM
    SSB.P_LINEORDER
WHERE
    LO_ORDERDATE = '1992-06-03';
```
:::info NOTE
The implicit date cast method relies on Kylin to automatically convert the string to a date. While this method is convenient, it may not always produce the desired results, especially when working with dates in different formats. The explicit date cast method provides more control over the conversion process.
:::
