---
title: Custom Parser Jar Management
language: en
sidebar_label: Custom Parser Jar Management
pagination_label: Custom Parser Jar Management
toc_min_heading_level: 2
toc_max_heading_level: 6
pagination_prev: null
pagination_next: null
keywords:
    - custom parser
    - jar management
draft: false
last_update:
    date: 09/03/2024
---
> Reminder:
>
> 1. Please read [Access and Authentication REST API](authentication.md) and understand how authentication works.
>
> 2. On Curl command line, don't forget to quote the URL if it contains the special char `&`.



* [Load Jar](#Load-Jar)
* [Delete Jar](#Delete-Jar)
* [Get Parser List](#Get-Parser-List)
* [Delete Parser](#Delete-Parser)

### Load Jar

- `POST http://host:port/kylin/api/custom/jar`

- HTTP Body: form-data

  - `project` - `required` `string`, project name

  - `file` - `required` `File`, jar file needs to be loaded

  - `jar_type` - `required` `string`, jar file type

    > Note: jar_type is only "STREAMING_CUSTOM_PARSER" for the parser.

- HTTP Header

  - `Accept: application/vnd.apache.kylin-v4-public+json`
  - `Accept-Language: en`
  - `Content-Type: multipart/form-data`

- Curl Request Example

  ```sh
    curl -X POST \
      'http://host:port/kylin/api/custom/jar' \
      -H 'Accept: application/vnd.apache.kylin-v4-public+json' \
      -H 'Accept-Language: en' \
      -H 'Content-Type: multipart/form-data' \
      -H 'Authorization: Basic QURNSU46S1lMSU4=' \
      -F 'file=@"/**/**/custom_parser.jar"' \
      -F 'project="TestProject"' \
      -F 'jar_type="STREAMING_CUSTOM_PARSER"'
  ```


- Response Details

    - `data`, Successfully loaded parser full path array

- Response Example

  ```json
  {
      "code": "000",
      "data": [
        "org.apache.kylin.parser.JsonDataParser1",
        "org.apache.kylin.parser.JsonDataParser2"
      ],
      "msg": ""
  }
  ```




### Delete Jar

- `DELETE http://host:port/kylin/api/custom/jar`

- URL Parameters

    - `project` - `required` `string`, project name

    - `jar_name` - `required` `string`, The file name of the JAR to delete

    - `jar_type` - `required` `string`, jar file type

      > Note: jar_type is only "STREAMING_CUSTOM_PARSER" for the parser.

- HTTP Header

    - `Accept: application/vnd.apache.kylin-v4-public+json`
    - `Accept-Language: en`
    - `Content-Type: application/json;charset=utf-8`

- Curl Request Example

  ```sh
    curl -X DELETE \
      'http://host:port/kylin/api/custom/jar?project=TestProject&jar_name=custom_parser1.jar&jar_type="STREAMING_CUSTOM_PARSER' \
      -H 'Accept: application/vnd.apache.kylin-v4-public+json' \
      -H 'Accept-Language: en' \
      -H 'Content-Type: application/json;charset=utf-8' \
      -H 'Authorization: Basic QURNSU46S1lMSU4='
  ```


- Response Details

    - `data`, Jar file name successfully deleted

- Response Example

  ```json
    {
        "code": "000",
        "data": "custom_parser1.jar",
        "msg": ""
    }
  ```




### Get Parser List

- `GET http://host:port/kylin/api/kafka/parsers`

- URL Parameters

    - `project` - `必选` `string`, project name


- HTTP Header

    - `Accept: application/vnd.apache.kylin-v4-public+json`
    - `Accept-Language: en`
    - `Content-Type: application/json;charset=utf-8`

- Curl Request Example

  ```sh
    curl -X GET \
      'http://host:port/kylin/api/kafka/parsers?project=TestProject' \
      -H 'Accept: application/vnd.apache.kylin-v4-public+json' \
      -H 'Accept-Language: en' \
      -H 'Content-Type: application/json;charset=utf-8' \
      -H 'Authorization: Basic QURNSU46S1lMSU4='
  ```


- Response Details

    - `data`, Loaded parser array

- Response Example

  ```json
    {
        "code": "000",
        "data": [
            "org.apache.kylin.parser.CsvDataParser2",
            "org.apache.kylin.parser.JsonDataParser2",
            "org.apache.kylin.parser.TimedJsonStreamParser"
        ],
        "msg": ""
    }
  ```




### Delete Parser

- `DELETE http://host:port/kylin/api/kafka/parser`

- URL Parameters

    - `project` - `必选` `string`, project name

    - `className` - `必选` `string`, The full path of the parser needs to be deleted

- HTTP Header

    - `Accept: application/vnd.apache.kylin-v4-public+json`
    - `Accept-Language: en`
    - `Content-Type: application/json;charset=utf-8`

- Curl Request Example

  ```sh
    curl -X DELETE \
      'http://host:port/kylin/api/kafka/parser?project=TestProject&className=org.apache.kylin.parser.JsonDataParser1' \
      -H 'Accept: application/vnd.apache.kylin-v4-public+json' \
      -H 'Accept-Language: en' \
      -H 'Content-Type: application/json;charset=utf-8' \
      -H 'Authorization: Basic QURNSU46S1lMSU4='
  ```


- Response Details

    - `data`, Parser full path successfully deleted

- Response Example

  ```json
    {
        "code": "000",
        "data": "org.apache.kylin.parser.JsonDataParser1",
        "msg": ""
    }
  ```






