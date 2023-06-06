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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.util.DateString;
import org.apache.calcite.util.TimeString;
import org.apache.calcite.util.TimestampString;
import org.apache.kylin.guava30.shaded.common.collect.ImmutableMap;
import org.apache.kylin.guava30.shaded.common.collect.Sets;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@Slf4j
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class NonEquiJoinCondition implements Serializable {

    private static final Map<SqlKind, TruthTable.Operator> OP_TRUTH_MAPPING = ImmutableMap.<SqlKind, TruthTable.Operator> builder()
            .put(SqlKind.AND, TruthTable.Operator.AND) //
            .put(SqlKind.OR, TruthTable.Operator.OR) //
            .put(SqlKind.NOT, TruthTable.Operator.NOT) //
            .build();

    private static final Map<SqlKind, SqlKind> OP_INVERSE_MAPPING = ImmutableMap.<SqlKind, SqlKind> builder()
            .put(SqlKind.NOT_EQUALS, SqlKind.EQUALS) //
            .put(SqlKind.NOT_IN, SqlKind.IN) //
            .put(SqlKind.IS_NOT_NULL, SqlKind.IS_NULL) //
            .put(SqlKind.IS_NOT_FALSE, SqlKind.IS_FALSE) //
            .put(SqlKind.IS_NOT_TRUE, SqlKind.IS_TRUE) //
            .put(SqlKind.IS_NOT_DISTINCT_FROM, SqlKind.IS_DISTINCT_FROM) //
            .put(SqlKind.LESS_THAN, SqlKind.GREATER_THAN_OR_EQUAL) //
            .put(SqlKind.LESS_THAN_OR_EQUAL, SqlKind.GREATER_THAN) //
            .build();

    @Getter
    @Setter
    @JsonProperty("type")
    private Type type;

    @Getter
    @Setter
    @JsonProperty("data_type")
    private DataType dataType; // data type of the corresponding rex node

    @Getter
    @Setter
    @JsonProperty("op")
    private SqlKind op; // kind of the operator

    @Getter
    @Setter
    @JsonProperty("op_name")
    private String opName; // name of the operator

    @Getter
    @Setter
    @JsonProperty("operands")
    private NonEquiJoinCondition[] operands = new NonEquiJoinCondition[0]; // nested operands

    @Getter
    @Setter
    @JsonProperty("value")
    private String value; // literal or column identity at leaf node

    @Getter
    @Setter
    private TblColRef colRef; // set at runtime with model init

    @Getter
    @Setter
    @JsonProperty("expr")
    private String expr;

    public NonEquiJoinCondition() {
    }

    static final Set<SqlKind> EXCHANGEABLE_OPERATOR = Sets.newHashSet(SqlKind.AND, SqlKind.OR, SqlKind.EQUALS,
            SqlKind.NOT_EQUALS, SqlKind.IN, SqlKind.NOT_IN);

    public NonEquiJoinCondition(SqlOperator op, NonEquiJoinCondition[] operands, RelDataType dataType) {
        this(op.getName(), op.getKind(), operands, new DataType(dataType));
    }

    public NonEquiJoinCondition(RexLiteral value, RelDataType dataType) {
        this(TypedLiteralConverter.typedLiteralToString(value), new DataType(dataType));
    }

    public NonEquiJoinCondition(TblColRef tblColRef, RelDataType dataType) {
        this(tblColRef, new DataType(dataType));
    }

    public NonEquiJoinCondition(String opName, SqlKind op, NonEquiJoinCondition[] operands, DataType dataType) {
        this.opName = opName;
        this.op = op;
        this.operands = operands;
        this.type = Type.EXPRESSION;
        this.dataType = dataType;
    }

    public NonEquiJoinCondition(String value, DataType dataType) {
        this.op = SqlKind.LITERAL;
        this.type = Type.LITERAL;
        this.value = value;
        this.dataType = dataType;
    }

    public NonEquiJoinCondition(TblColRef tblColRef, DataType dataType) {
        this.op = SqlKind.INPUT_REF;
        this.type = Type.COLUMN;
        this.value = tblColRef.getIdentity();
        this.colRef = tblColRef;
        this.dataType = dataType;
    }

    public Object getTypedValue() {
        return TypedLiteralConverter.stringValueToTypedValue(value, dataType);
    }

    public NonEquiJoinCondition copy() {
        NonEquiJoinCondition condCopy = new NonEquiJoinCondition();
        condCopy.type = type;
        condCopy.dataType = dataType;
        condCopy.op = op;
        condCopy.opName = opName;
        condCopy.operands = Arrays.copyOf(operands, operands.length);
        condCopy.value = value;
        condCopy.colRef = colRef;
        condCopy.expr = expr;
        return condCopy;
    }

    public NonEquiJoinCondition[] getSortedOperands() {
        if (EXCHANGEABLE_OPERATOR.contains(op)) {
            return Arrays.stream(operands).sorted(Comparator.comparing(NonEquiJoinCondition::getOp))
                    .toArray(NonEquiJoinCondition[]::new);
        } else {
            return Arrays.copyOf(operands, operands.length);
        }
    }

    private TruthTable<NonEquiJoinCondition> createTruthTable() {
        TruthTable.ExprBuilder<NonEquiJoinCondition> builder = new TruthTable.ExprBuilder<>(
                new NeqCondOperandComparator());
        this.accept(builder);
        TruthTable.Expr<NonEquiJoinCondition> builtExpr = builder.build();
        return new TruthTable<>(new ArrayList<>(builder.allOperandSet), builtExpr);
    }

    private void accept(TruthTable.ExprBuilder<NonEquiJoinCondition> builder) {
        switch (op) {
        case AND:
        case NOT:
        case OR:
            builder.compositeStart(OP_TRUTH_MAPPING.get(op));
            for (NonEquiJoinCondition operand : operands) {
                operand.accept(builder);
            }
            builder.compositeEnd();
            break;
        default:
            this.addOperand(builder);
            break;
        }
    }

    private void addOperand(TruthTable.ExprBuilder<NonEquiJoinCondition> builder) {
        NonEquiJoinCondition copied = this.copy();
        if (inverseCondOperator(copied)) {
            builder.compositeStart(TruthTable.Operator.NOT);
            normalizedCondOperandOrderings(copied);
            builder.addExpr(copied);
            builder.compositeEnd();
        } else {
            normalizedCondOperandOrderings(copied);
            builder.addExpr(copied);
        }
    }

    private boolean inverseCondOperator(NonEquiJoinCondition neqJoinCond) {
        if (OP_INVERSE_MAPPING.containsKey(neqJoinCond.getOp())) {
            neqJoinCond.setOp(OP_INVERSE_MAPPING.get(neqJoinCond.getOp()));
            neqJoinCond.setOpName(neqJoinCond.getOp().sql);
            return true;
        }
        return false;
    }

    private void normalizedCondOperandOrderings(NonEquiJoinCondition neqJoinCond) {
        // if operands ordering does not matter, sort by operands' digest
        // case =, <>
        if (neqJoinCond.getOp() == SqlKind.EQUALS) {
            Arrays.sort(neqJoinCond.getOperands(), Comparator.comparing(NonEquiJoinCondition::toString));
        }

        // sort IN args
        if (neqJoinCond.getOp() == SqlKind.IN) {
            Arrays.sort(neqJoinCond.getOperands(), 1, neqJoinCond.getOperands().length,
                    Comparator.comparing(NonEquiJoinCondition::toString));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(op);
        sb.append("(");
        NonEquiJoinCondition[] sorted = getSortedOperands();
        for (NonEquiJoinCondition input : sorted) {
            sb.append(input.toString());
            sb.append(", ");
        }
        if (type == Type.LITERAL) {
            sb.append(value);
        } else if (type == Type.COLUMN) {
            if (colRef != null) {
                sb.append(colRef.getColumnWithTableAndSchema());
            } else {
                sb.append(value);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NonEquiJoinCondition)) {
            return false;
        }

        NonEquiJoinCondition neqJoinCond = (NonEquiJoinCondition) obj;
        try {
            return Objects.equals(this.createTruthTable(), neqJoinCond.createTruthTable());
        } catch (Exception e) {
            log.error("Error on comparing condition({}), condition({}). Error Msg: {}", this, neqJoinCond,
                    e.getMessage());
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        result = prime * result + dataType.hashCode();
        result = prime * result + op.hashCode();
        // consider opName only SqlKind OTHER
        if ((op == SqlKind.OTHER || op == SqlKind.OTHER_FUNCTION) && opName != null) {
            result = prime * result + opName.hashCode();
        }
        for (NonEquiJoinCondition operand : operands) {
            result = prime * result + operand.hashCode();
        }

        if (type == Type.LITERAL) {
            result = prime * result + value.hashCode();
        } else if (type == Type.COLUMN) {
            if (colRef != null) {
                result = prime * result + colRef.hashCode();
            } else {
                result = prime * result + value.hashCode();
            }
        }
        return result;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static final class SimplifiedJoinCondition implements Serializable {

        private static final long serialVersionUID = -1577556052145832500L;

        @JsonProperty("foreign_key")
        private String foreignKey;

        @JsonProperty("primary_key")
        private String primaryKey;

        @JsonProperty("op")
        private SqlKind op;

        public SimplifiedJoinCondition(String foreignKey, String primaryKey, SqlKind op) {
            this.foreignKey = foreignKey;
            this.primaryKey = primaryKey;
            this.op = op;
        }

        public SimplifiedJoinCondition(String foreignKey, TblColRef fk, String primaryKey, TblColRef pk, SqlKind op) {
            this.foreignKey = foreignKey;
            this.primaryKey = primaryKey;
            this.op = op;
            this.fk = fk;
            this.pk = pk;
        }

        @JsonIgnore
        private TblColRef fk;

        @JsonIgnore
        private TblColRef pk;

        public String displaySql() {
            return fk.getDoubleQuoteExp() + " " + op.sql + " " + pk.getDoubleQuoteExp();
        }
    }

    static class NeqCondOperandComparator implements Comparator<NonEquiJoinCondition> {
        @Override
        public int compare(NonEquiJoinCondition cond1, NonEquiJoinCondition cond2) {
            if (!(Objects.equals(cond1.getOp(), cond2.getOp())
                    && cond1.getOperands().length == cond2.getOperands().length
                    && Objects.equals(cond1.getType(), cond2.getType())
                    && Objects.equals(cond1.getDataType(), cond2.getDataType()))) {
                return 1;
            }

            // compare opName on for SqlKind OTHER
            if (cond1.getOp() == SqlKind.OTHER || cond1.getOp() == SqlKind.OTHER_FUNCTION
                    && (!Objects.equals(cond1.getOpName(), cond2.getOpName()))) {
                return 1;
            }

            if (cond1.getType() == Type.LITERAL) {
                return Objects.equals(cond1.getValue(), cond2.getValue()) ? 0 : 1;
            } else if (cond1.getType() == Type.COLUMN) {
                return Objects.equals(cond1.getColRef().getColumnDesc(), cond2.getColRef().getColumnDesc()) ? 0 : 1;
            }
            NonEquiJoinCondition[] sorted1 = cond1.getSortedOperands();
            NonEquiJoinCondition[] sorted2 = cond2.getSortedOperands();
            for (int i = 0; i < sorted1.length; i++) {
                if (compare(sorted1[i], sorted2[i]) != 0) {
                    return 1;
                }
            }
            return 0;
        }
    }

    public interface NeqConditionVisitor {

        default NonEquiJoinCondition visit(NonEquiJoinCondition neqJoinCond) {
            if (neqJoinCond == null) {
                return null;
            }

            if (neqJoinCond.getType() == Type.LITERAL) {
                return visitLiteral(neqJoinCond);
            } else if (neqJoinCond.getType() == Type.COLUMN) {
                return visitColumn(neqJoinCond);
            } else if (neqJoinCond.getType() == Type.EXPRESSION) {
                return visitExpression(neqJoinCond);
            } else {
                return visit(neqJoinCond);
            }
        }

        default NonEquiJoinCondition visitExpression(NonEquiJoinCondition neqJoinCond) {
            NonEquiJoinCondition[] ops = new NonEquiJoinCondition[neqJoinCond.getOperands().length];
            for (int i = 0; i < neqJoinCond.getOperands().length; i++) {
                ops[i] = visit(neqJoinCond.getOperands()[i]);
            }
            neqJoinCond.setOperands(ops);
            return neqJoinCond;
        }

        default NonEquiJoinCondition visitColumn(NonEquiJoinCondition neqJoinCond) {
            return neqJoinCond;
        }

        default NonEquiJoinCondition visitLiteral(NonEquiJoinCondition neqJoinCond) {
            return neqJoinCond;
        }
    }

    public static class TypedLiteralConverter {
        private TypedLiteralConverter() {
        }

        static final String NULL = "KY_LITERAL_NULL";

        public static Object stringValueToTypedValue(String value, DataType dataType) {
            if (value.equals(NULL)) {
                return null;
            }
            switch (dataType.getTypeName()) {
            case DECIMAL:
                return new BigDecimal(value);
            case BIGINT:
                return Long.parseLong(value);
            case SMALLINT:
                return Short.parseShort(value);
            case TINYINT:
                return Byte.parseByte(value);
            case INTEGER:
                return Integer.parseInt(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case FLOAT:
            case REAL:
                return Float.parseFloat(value);
            case DATE:
                return DateString.fromDaysSinceEpoch(Integer.parseInt(value));
            case TIME:
            case TIME_WITH_LOCAL_TIME_ZONE:
                return TimeString.fromMillisOfDay(Integer.parseInt(value));
            case TIMESTAMP:
            case TIMESTAMP_WITH_LOCAL_TIME_ZONE:
                return TimestampString.fromMillisSinceEpoch(Long.parseLong(value));
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case CHAR:
            case VARCHAR:
            default:
                return value;
            }
        }

        public static String typedLiteralToString(RexLiteral literal) {
            if (literal.getValue3() == null) {
                return NULL;
            }
            return String.valueOf(literal.getValue3());
        }
    }

    public enum Type {
        /**
         * A sub NonEquiJoinCondition expression.
         */
        EXPRESSION,
        /**
         * A column.
         */
        COLUMN,
        /**
         * A literal, unlikely type for scd2 join condition.
         */
        LITERAL
    }
}
