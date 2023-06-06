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

package org.apache.kylin.metadata.model;

import java.math.BigDecimal;
import java.util.function.Function;

import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.TimestampString;
import org.apache.kylin.common.util.DateFormat;
import org.apache.kylin.metadata.model.NonEquiJoinCondition.TypedLiteralConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NeqEquiJoinConditionTest {

    ModelNonEquiCondMock nonEquiMock = new ModelNonEquiCondMock();

    @AfterEach
    public void teardown() {
        nonEquiMock.clearTableRefCache();
    }

    @Test
    void testSimpleEqual() {
        NonEquiJoinCondition cond1 = nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a1", "B.b1");
        Assertions.assertNotNull(cond1);
    }

    @Test
    void testNestedCondEqual() {
        NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.OR,
                nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a1", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a2", "B.b1"),
                ModelNonEquiCondMock.composite(SqlKind.AND,
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a3", "B.b2"),
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a3", "B.b2")),
                ModelNonEquiCondMock.composite(SqlKind.NOT, nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a5", "B.b6")),
                nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a9", "B.b8"), nonEquiMock.colConstantCompareCond(
                        SqlKind.EQUALS, "A.a9", SqlTypeName.CHAR, "SOME TEXT", SqlTypeName.CHAR));
        Assertions.assertNotNull(cond1);
    }

    @Test
    void testOrderingIrrelevantCondEqual() {
        NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.OR,
                nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a1", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a2", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a9", "B.b8"));
        NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.OR,
                nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a9", "B.b8"),
                nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a1", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a2", "B.b1"));
        Assertions.assertEquals(cond1, cond2);
    }

    @Test
    void testComparingWithNull() {
        NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.OR,
                nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a1", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a2", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a9", "B.b8"));
        Assertions.assertNotNull(cond1);
    }

    @Test
    void testNotEqual() {
        NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.OR,
                nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a1", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a2", "B.b1"),
                ModelNonEquiCondMock.composite(SqlKind.AND,
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a3", "B.b2"),
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a3", "B.b2")),
                ModelNonEquiCondMock.composite(SqlKind.NOT, nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a5", "B.b6")),
                nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a9", "B.b8"), nonEquiMock.colConstantCompareCond(
                        SqlKind.EQUALS, "A.a9", SqlTypeName.CHAR, "SOME TEXT", SqlTypeName.CHAR));
        NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.OR,
                nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a1", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a2", "B.b1"),
                ModelNonEquiCondMock.composite(SqlKind.OR,
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a3", "B.b8"),
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a3", "B.b2")),
                ModelNonEquiCondMock.composite(SqlKind.NOT, nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a5", "B.b6")),
                nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a9", "B.b8"), nonEquiMock.colConstantCompareCond(
                        SqlKind.EQUALS, "A.a9", SqlTypeName.CHAR, "SOME TEXT", SqlTypeName.CHAR));
        Assertions.assertNotEquals(cond1, cond2);
    }

    @Test
    void testNotEqualOnTable() {
        NonEquiJoinCondition cond1 = nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a1", "B.b1");
        NonEquiJoinCondition cond2 = nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a1", "C.b1");
        Assertions.assertNotEquals(cond1, cond2);
    }

    @Test
    void testNotEqualOnColumn() {
        NonEquiJoinCondition cond1 = nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a1", "B.b1");
        NonEquiJoinCondition cond2 = nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "A.a1", "B.b2");
        Assertions.assertNotEquals(cond1, cond2);
    }

    @Test
    void testNotEqualOnConstant() {
        NonEquiJoinCondition cond1 = nonEquiMock.colConstantCompareCond(SqlKind.EQUALS, "A.a9", SqlTypeName.CHAR,
                "SOME TEXT", SqlTypeName.CHAR);
        NonEquiJoinCondition cond2 = nonEquiMock.colConstantCompareCond(SqlKind.EQUALS, "A.a9", SqlTypeName.CHAR,
                "SOME TEXT1", SqlTypeName.CHAR);
        Assertions.assertNotEquals(cond1, cond2);
    }

    @Test
    void testNotEqualOnType() {
        NonEquiJoinCondition cond1 = nonEquiMock.colConstantCompareCond(SqlKind.EQUALS, "A.a9", SqlTypeName.CHAR,
                "SOME TEXT", SqlTypeName.CHAR);
        NonEquiJoinCondition cond2 = nonEquiMock.colConstantCompareCond(SqlKind.EQUALS, "A.a9", SqlTypeName.CHAR,
                "SOME TEXT", SqlTypeName.INTEGER);
        Assertions.assertNotEquals(cond1, cond2);
    }

    @Test
    void testCondNormalizations() {
        NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.AND,
                nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a8", "B.b8"),
                ModelNonEquiCondMock.composite(SqlKind.NOT, nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a9", "B.b9")),
                ModelNonEquiCondMock.composite(SqlKind.NOT, nonEquiMock.colOp(SqlKind.IS_NULL, "A.a9")),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN_OR_EQUAL, "A.a1", "B.b1"),
                nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a4", "B.b4"));
        NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.AND,
                nonEquiMock.colCompareCond(SqlKind.EQUALS, "B.b8", "A.a8"),
                nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "B.b9", "A.a9"),
                nonEquiMock.colOp(SqlKind.IS_NOT_NULL, "A.a9"),
                ModelNonEquiCondMock.composite(SqlKind.NOT,
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a1", "B.b1")),
                ModelNonEquiCondMock.composite(SqlKind.NOT,
                        nonEquiMock.colCompareCond(SqlKind.GREATER_THAN_OR_EQUAL, "A.a4", "B.b4")));
        Assertions.assertEquals(cond1, cond2);
    }

    @Test
    void testCondNormalizationsOnSqlKindOTHER() {
        NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "A.a9"),
                nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"));
        NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"),
                nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "A.a9"));
        Assertions.assertEquals(cond1, cond2);
    }

    @Test
    void testCondNormalizationsOnSqlKindOTHERNotEqual() {
        NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "A.a9"),
                nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"));
        NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"),
                nonEquiMock.colOp("cos", SqlKind.OTHER_FUNCTION, "A.a9"));
        Assertions.assertNotEquals(cond1, cond2);
    }

    @Test
    void testNonEquJoinDescComparator() {

        Function<NonEquiJoinCondition, JoinDesc> supplierSimpleJoinDesc = (nonEquiJoinCondition) -> {
            JoinDesc joinDesc = new JoinDesc.NonEquivJoinDesc();
            joinDesc.setNonEquiJoinCondition(nonEquiJoinCondition);
            joinDesc.setPrimaryKey(new String[] {});
            joinDesc.setForeignKey(new String[] {});
            joinDesc.setForeignKeyColumns(new TblColRef[] {});
            joinDesc.setPrimaryKeyColumns(new TblColRef[] {});
            joinDesc.setType("LEFT");
            return joinDesc;
        };
        //nonEqual
        {
            NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                    nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "A.a9"),
                    nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"));
            NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                    nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"),
                    nonEquiMock.colOp("cos", SqlKind.OTHER_FUNCTION, "A.a9"));
            Assertions.assertNotEquals(supplierSimpleJoinDesc.apply(cond1), supplierSimpleJoinDesc.apply(cond2));
        }

        //equal
        {
            NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                    nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "A.a9"),
                    nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"));
            NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.EQUALS,
                    nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "B.a9"),
                    nonEquiMock.colOp("abs", SqlKind.OTHER_FUNCTION, "A.a9"));
            Assertions.assertEquals(supplierSimpleJoinDesc.apply(cond1), supplierSimpleJoinDesc.apply(cond2));
        }
        //equal
        {
            NonEquiJoinCondition cond1 = ModelNonEquiCondMock.composite(SqlKind.AND,
                    nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a8", "B.b8"),
                    ModelNonEquiCondMock.composite(SqlKind.NOT,
                            nonEquiMock.colCompareCond(SqlKind.EQUALS, "A.a9", "B.b9")),
                    ModelNonEquiCondMock.composite(SqlKind.NOT, nonEquiMock.colOp(SqlKind.IS_NULL, "A.a9")),
                    nonEquiMock.colCompareCond(SqlKind.LESS_THAN_OR_EQUAL, "A.a1", "B.b1"),
                    nonEquiMock.colCompareCond(SqlKind.LESS_THAN, "A.a4", "B.b4"));
            NonEquiJoinCondition cond2 = ModelNonEquiCondMock.composite(SqlKind.AND,
                    nonEquiMock.colCompareCond(SqlKind.EQUALS, "B.b8", "A.a8"),
                    nonEquiMock.colCompareCond(SqlKind.NOT_EQUALS, "B.b9", "A.a9"),
                    nonEquiMock.colOp(SqlKind.IS_NOT_NULL, "A.a9"),
                    ModelNonEquiCondMock.composite(SqlKind.NOT,
                            nonEquiMock.colCompareCond(SqlKind.GREATER_THAN, "A.a1", "B.b1")),
                    ModelNonEquiCondMock.composite(SqlKind.NOT,
                            nonEquiMock.colCompareCond(SqlKind.GREATER_THAN_OR_EQUAL, "A.a4", "B.b4")));
            Assertions.assertEquals(supplierSimpleJoinDesc.apply(cond1), supplierSimpleJoinDesc.apply(cond2));
        }
    }

    @Test
    void testTypedLiteralConverter() {
        DataType anyType = ModelNonEquiCondMock.mockDataType(SqlTypeName.ANY);
        Assertions.assertNull(TypedLiteralConverter.stringValueToTypedValue(TypedLiteralConverter.NULL, anyType));

        DataType decimalType = ModelNonEquiCondMock.mockDataType(SqlTypeName.DECIMAL);
        BigDecimal decimal = new BigDecimal("1.5");
        Assertions.assertEquals(decimal, TypedLiteralConverter.stringValueToTypedValue("1.5", decimalType));

        DataType longType = ModelNonEquiCondMock.mockDataType(SqlTypeName.BIGINT);
        Assertions.assertEquals(1L, TypedLiteralConverter.stringValueToTypedValue("1", longType));

        DataType sType = ModelNonEquiCondMock.mockDataType(SqlTypeName.SMALLINT);
        Assertions.assertEquals((short) 1, TypedLiteralConverter.stringValueToTypedValue("1", sType));

        DataType tinyInt = ModelNonEquiCondMock.mockDataType(SqlTypeName.TINYINT);
        Assertions.assertEquals((byte) 1, TypedLiteralConverter.stringValueToTypedValue("1", tinyInt));

        DataType intType = ModelNonEquiCondMock.mockDataType(SqlTypeName.INTEGER);
        Assertions.assertEquals(1, TypedLiteralConverter.stringValueToTypedValue("1", intType));

        DataType dType = ModelNonEquiCondMock.mockDataType(SqlTypeName.DOUBLE);
        Assertions.assertEquals(1.0d, TypedLiteralConverter.stringValueToTypedValue("1.0", dType));

        DataType fType = ModelNonEquiCondMock.mockDataType(SqlTypeName.FLOAT);
        Assertions.assertEquals(1.0f, TypedLiteralConverter.stringValueToTypedValue("1", fType));

        DataType realType = ModelNonEquiCondMock.mockDataType(SqlTypeName.REAL);
        Assertions.assertEquals(1.0f, TypedLiteralConverter.stringValueToTypedValue("1", realType));

        DataType boolType = ModelNonEquiCondMock.mockDataType(SqlTypeName.BOOLEAN);
        Assertions.assertEquals(false, TypedLiteralConverter.stringValueToTypedValue("false", boolType));

        DataType charType = ModelNonEquiCondMock.mockDataType(SqlTypeName.CHAR);
        Assertions.assertEquals("a", TypedLiteralConverter.stringValueToTypedValue("a", charType));

        DataType varcharType = ModelNonEquiCondMock.mockDataType(SqlTypeName.VARCHAR);
        Assertions.assertEquals("abc", TypedLiteralConverter.stringValueToTypedValue("abc", varcharType));

        // why timezone problem?
        String tsValue = "2023-01-03 12:13:14";
        long ts = DateFormat.stringToMillis(tsValue);
        TimestampString expTs = new TimestampString(2023, 1, 3, 4, 13, 14);
        DataType tsType = ModelNonEquiCondMock.mockDataType(SqlTypeName.TIMESTAMP);
        Assertions.assertEquals(expTs, TypedLiteralConverter.stringValueToTypedValue(String.valueOf(ts), tsType));
    }
}
