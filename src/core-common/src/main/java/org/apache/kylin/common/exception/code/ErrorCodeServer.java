/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kylin.common.exception.code;

import org.apache.commons.lang3.StringUtils;

public enum ErrorCodeServer implements ErrorCodeProducer {

    // 100012XX project
    PROJECT_NOT_EXIST("KE-010001201"),
    PROJECT_MULTI_PARTITION_DISABLE("KE-010001208"),
    PROJECT_SCD2_IS_NOT_ALLOWED("KE-010001209"),

    // 100022XX model
    MODEL_NOT_EXIST("KE-010002201"),
    MODEL_ID_NOT_EXIST("KE-010002202"),
    MODEL_NAME_NOT_EXIST("KE-010002203"),
    MODEL_NAME_EMPTY("KE-010002204"),
    MODEL_NAME_INVALID("KE-010002205"),
    MODEL_NAME_DUPLICATE("KE-010002206"),
    SIMPLIFIED_MEASURES_MISSING_ID("KE-010002207"),
    MODEL_NOT_EXIST_SEGMENTS("KE-010002208"),
    MODEL_TDS_EXPORT_DIM_COL_AND_MEASURE_NAME_CONFLICT("KE-010002301"),
    MODEL_TDS_EXPORT_COLUMN_AND_MEASURE_NAME_CONFLICT("KE-010002302"),
    MODEL_SUM_LC_INVALID_DATA_TYPE("KE-010002303"),
    MODEL_SUM_LC_INVALID_TIMESTAMP_TYPE("KE-010002304"),
    MODEL_NAME_TOO_LONG("KE-010002305"),
    MODEL_SECOND_STORAGE_PARTITION_INVALID("KE-010002306"),
    PARTITION_SECOND_STORAGE_PARTITION_INVALID("KE-010002307"),
    SCD2_MODEL_CAN_ONLY_CONNECT_BY_AND("KE-010002401"),
    SCD2_MODEL_CONTAINS_ILLEGAL_EXPRESSIONS("KE-010002402"),
    SCD2_CONDITION_MUST_APPEAR_IN_PAIRS("KE-010002403"),
    SCD2_MODEL_REQUIRES_AT_LEAST_ONE_EQUAL_CONDITION("KE-010002404"),
    SCD2_MODEL_REQUIRES_AT_LEAST_ONE_NON_EQUAL_CONDITION("KE-010002405"),
    SCD2_MODEL_PK_FK_UNIQUE_CHECK_FAILED("KE-010002406"),
    DUPLICATE_MODEL_JOIN_CONDITIONS("KE-010002407"),
    SCD2_MODEL_UNKNOWN_EXCEPTION("KE-010002408"),

    // 100252XX Cube
    CUBE_NOT_EXIST("KE-010025201"),

    // 100222XX Segment
    SEGMENT_REFRESH_INVALID_RANGE("KE-010022201"),
    SEGMENT_BUILD_RANGE_OVERLAP("KE-010022202"),
    SEGMENT_REFRESH_SELECT_EMPTY("KE-010022203"),
    SEGMENT_REFRESH_IN_BUILDING("KE-010022204"),
    SEGMENT_REFRESH_SELECT_RANGE_EMPTY("KE-010022205"),
    SEGMENT_REFRESH_MORE_THAN_ONE("KE-010022206"),
    SEGMENT_MERGE_LESS_THAN_TWO("KE-010022207"),
    SEGMENT_MERGE_CONTAINS_GAPS("KE-010022208"),
    SEGMENT_NOT_EXIST_NAME("KE-010022209"),
    SEGMENT_NOT_EXIST_ID("KE-010022210"),
    SEGMENT_EMPTY_ID("KE-010022211"),
    SEGMENT_SELECT_EMPTY("KE-010022212"),
    SEGMENT_DELETE_SELECT_EMPTY("KE-010022213"),
    SEGMENT_CONFLICT_PARAMETER("KE-010022214"),
    SEGMENT_EMPTY_PARAMETER("KE-010022215"),
    SEGMENT_LOCKED("KE-010022216"),
    SEGMENT_STATUS("KE-010022217"),
    SEGMENT_MERGE_CHECK_INDEX_ILLEGAL("KE-010022218"),
    SEGMENT_MERGE_CHECK_PARTITION_ILLEGAL("KE-010022219"),
    SEGMENT_INDEX_CONFLICT_PARAMETER("KE-010022220"),
    SEGMENT_INDEX_STATUS_INVALID("KE-010022221"),
    SEGMENT_SINGLE_JOB_THRESHOLD("KE-010022222"),
    SEGMENT_SECOND_STORAGE_PARTITION_INVALID("KE-010022223"),

    // 100072XX table
    TABLE_RELOAD_MODEL_RETRY("KE-010007204"),
    TABLE_RELOAD_HAVING_NOT_FINAL_JOB("KE-010007208"),
    EXCLUDED_TABLE_REQUEST_NOT_ALLOWED("KE-010007301"),
    ONCE_LOAD_TABLE_LIMIT("KE-010007011"),

    // 10032XXX add job result
    JOB_CREATE_CHECK_MULTI_PARTITION_EMPTY("KE-010032201"),
    JOB_CREATE_CHECK_INDEX_FAIL("KE-010032202"),
    JOB_CREATE_CHECK_SEGMENT_READY_FAIL("KE-010032203"),
    JOB_CREATE_EXCEPTION("KE-010032204"),
    JOB_CREATE_CHECK_MULTI_PARTITION_ABANDON("KE-010032205"),
    JOB_CREATE_CHECK_FAIL("KE-010032206"),
    JOB_CREATE_CHECK_SEGMENT_FAIL("KE-010032207"),
    JOB_CREATE_ABANDON("KE-010032208"),
    JOB_CREATE_CHECK_MULTI_PARTITION_DUPLICATE("KE-010032209"),
    JOB_REFRESH_CHECK_INDEX_FAIL("KE-010032210"),
    JOB_UPDATE_STATUS_FAILED("KE-010032211"),
    JOB_STATUS_ILLEGAL("KE-010032212"),
    JOB_ID_EMPTY("KE-010032213"),
    JOB_SAMPLING_RANGE_INVALID("KE-010032214"),
    JOB_STATE_TRANSFER_ILLEGAL("KE-010032215"),
    JOB_CONCURRENT_SUBMIT_LIMIT("KE-010032216"),
    JOB_ACTION_ILLEGAL("KE-010032217"),
    JOB_STORAGE_QUOTA_LIMIT("KE-010032218"),
    JOB_NOT_EXIST("KE-010032219"),
    JOB_RESTART_CHECK_SEGMENT_STATUS("KE-010032220"),
    JOB_STOP_CHECK_SERVERLESS_JOB_STATUS_FAILED("KE-010032221"),

    // 100032XX user
    USER_UNAUTHORIZED("KE-010003207"),
    USER_LOGIN_FAILED("KE-010003208"),
    USER_AUTH_INFO_NOTFOUND("KE-010003209"),

    // 100312XX query
    QUERY_RESULT_OBTAIN_FAILED("KE-010031201"),
    INVALID_SQL_FORMAT("KE-010031202"),

    // 100102XX computed column
    COMPUTED_COLUMN_CONFLICT("KE-010010201"),
    COMPUTED_COLUMN_NAME_CONFLICT("KE-010010202"),
    COMPUTED_COLUMN_EXPR_CONFLICT("KE-010010203"),
    COMPUTED_COLUMN_CONFLICT_ADJUST_INFO("KE-010010204"),
    COMPUTED_COLUMN_NAME_OR_EXPR_EMPTY("KE-010010205"),

    // 100122XX index
    RULE_BASED_INDEX_METADATA_INCONSISTENT("KE-010012201"),
    INDEX_DUPLICATE("KE-010012202"),
    INDEX_PARAMETER_INVALID("KE-010012203"),
    SHARD_BY_COLUMN_NOT_IN_INDEX("KE-010012204"),
    OUT_OF_MAX_DIM_COMBINATION("KE-010012205"),
    DIMENSION_NOT_IN_MODEL("KE-010012206"),
    MEASURE_NOT_IN_MODEL("KE-010012207"),
    MANDATORY_NOT_IN_DIMENSION("KE-010012208"),
    HIERARCHY_NOT_IN_DIMENSION("KE-010012209"),
    JOINT_NOT_IN_DIMENSION("KE-010012210"),
    DIMENSION_ONLY_SET_ONCE("KE-010012211"),
    BASE_TABLE_INDEX_DELETE_DISABLE("KE-010012212"),

    // 10043XX parameter check
    REQUEST_PARAMETER_EMPTY_OR_VALUE_EMPTY("KE-010043201"),
    TIME_INVALID_RANGE_LESS_THAN_ZERO("KE-010043202"),
    TIME_INVALID_RANGE_END_LESS_THAN_EQUALS_START("KE-010043203"),
    TIME_INVALID_RANGE_END_LESS_THAN_START("KE-010043204"),
    TIME_INVALID_RANGE_NOT_FORMAT_MS("KE-010043205"),
    TIME_INVALID_RANGE_NOT_CONSISTENT("KE-010043206"),
    PARAMETER_INVALID_SUPPORT_LIST("KE-010043207"),
    INTEGER_NON_NEGATIVE_CHECK("KE-010043208"),
    ARGS_TYPE_CHECK("KE-010043209"),
    DATETIME_FORMAT_EMPTY("KE-010043210"),
    DATETIME_FORMAT_PARSE_ERROR("KE-010043211"),
    LAYOUT_LIST_EMPTY("KE-010043212"),
    LAYOUT_NOT_EXISTS("KE-010043213"),
    STREAMING_TABLE_REFRESH_INTERVAL_UNIT_ERROR("KE-010043214"),
    CONFIG_NOT_SUPPORT_EDIT("KE-010043215"),
    CONFIG_NOT_SUPPORT_DELETE("KE-010043216"),
    TIME_INVALID_RANGE_IN_RANGE("KE-010043217"),
    SORT_BY_FIELD_NOT_EXIST("KE-010043218"),
    USERNAME_COMPANY_NAME_INVALID_VALUE("KE-010043219"),
    USER_GROUP_NOT_EXIST("KE-010043220"),
    REPEATED_PARAMETER("KE-010043221"),
    INTEGER_POSITIVE_CHECK("KE-010043222"),

    // 100313xx async query
    ASYNC_QUERY_RESULT_NOT_FOUND("KE-010031301"),
    ASYNC_QUERY_PROJECT_NAME_EMPTY("KE-010031302"),
    ASYNC_QUERY_TIME_FORMAT_ERROR("KE-010031303"),
    ASYNC_QUERY_INCLUDE_HEADER_NOT_EMPTY("KE-010031304"),
    ASYNC_QUERY_OUT_OF_DATA_RANGE("KE-010031305"),

    // 400272XX resource group
    RESOURCE_GROUP_DISABLE_FAILED("KE-040027201"),
    RESOURCE_GROUP_ENABLE_FAILED("KE-040027202"),
    RESOURCE_GROUP_ID_EMPTY("KE-040027203"),
    RESOURCE_GROUP_ID_ALREADY_EXIST("KE-040027204"),
    RESOURCE_GROUP_INCOMPLETE_PARAMETER_LIST("KE-040027205"),
    RESOURCE_GROUP_ID_NOT_FOUND_IN_INSTANCE("KE-040027206"),
    REPEATED_INSTANCE("KE-040027207"),
    PARAMETER_IN_PARAMETER_NOT_EMPTY("KE-040027208"),
    RESOURCE_GROUP_BINDING_PROJECT_INVALID("KE-040027209"),
    RESOURCE_GROUP_ID_NOT_EXIST_IN_MAPPING_INFO("KE-040027210"),

    // 100352XX Streaming
    STREAMING_PARSE_MESSAGE_ERROR("KE-010035202"),
    READ_KAFKA_JAAS_FILE_ERROR("KE-010035215"),
    KAFKA_JAAS_FILE_KEYTAB_NOT_EXISTS("KE-010035216"),
    KAFKA_JAAS_FILE_KAFKACLIENT_NOT_EXISTS("KE-010035217"),

    // 100422XX CUSTOM PARSER
    CUSTOM_PARSER_NOT_JAR("KE-010042201"),
    CUSTOM_PARSER_JAR_EXISTS("KE-010042202"),
    CUSTOM_PARSER_UPLOAD_JAR_FAILED("KE-010042203"),
    CUSTOM_PARSER_JAR_PARSERS_NOT_EXISTS("KE-010042204"),
    CUSTOM_PARSER_PARSER_EXISTS("KE-010042205"),
    CUSTOM_PARSER_TABLES_USE_JAR("KE-010042206"),
    CUSTOM_PARSER_TABLES_USE_PARSER("KE-010042207"),
    CUSTOM_PARSER_CHECK_COLUMN_NAME_FAILED("KE-010042208"),
    CUSTOM_PARSER_UPLOAD_PARSER_LIMIT("KE-010042209"),
    CUSTOM_PARSER_CANNOT_DELETE_DEFAULT_PARSER("KE-010042210"),
    CUSTOM_PARSER_LOAD_JAR_FAILED("KE-010042211"),
    CUSTOM_PARSER_NOT_EXISTS_PARSER("KE-010042212"),
    CUSTOM_PARSER_JAR_TOO_LARGE("KE-010042213"),
    CUSTOM_PARSER_NOT_EXISTS_JAR("KE-010042214"),
    CUSTOM_PARSER_ALREADY_EXISTS_PARSER("KE-010042215"),
    CUSTOM_PARSER_ALREADY_EXISTS_JAR("KE-010042216");

    public static ErrorCodeServer of(String keCode) {
        if (StringUtils.isBlank(keCode)) {
            return null;
        }
        for (ErrorCodeServer value : values()) {
            if (value.getErrorCode().getCode().equals(keCode)) {
                return value;
            }
        }
        return null;
    }

    private final ErrorCode errorCode;
    private final ErrorMsg errorMsg;
    private final ErrorSuggestion errorSuggestion;

    ErrorCodeServer(String keCode) {
        this.errorCode = new ErrorCode(keCode);
        this.errorMsg = new ErrorMsg(this.errorCode.getCode());
        this.errorSuggestion = new ErrorSuggestion(this.errorCode.getCode());
    }

    @Override
    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    @Override
    public ErrorMsg getErrorMsg() {
        return this.errorMsg;
    }

    @Override
    public ErrorSuggestion getErrorSuggest() {
        return this.errorSuggestion;
    }
}
