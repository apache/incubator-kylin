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

package org.apache.kylin.query.schema;

import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.type.SqlTypeUtil;

public class KylinJavaTypeFactoryImpl extends JavaTypeFactoryImpl {

    private static final int MINIMUM_ADJUSTED_SCALE = 6;

    @Override
    public RelDataType createDecimalQuotient(RelDataType type1, RelDataType type2) {
        if (SqlTypeUtil.isExactNumeric(type1) && SqlTypeUtil.isExactNumeric(type2)
                && (SqlTypeUtil.isDecimal(type1) || SqlTypeUtil.isDecimal(type2))) {
            int p1 = type1.getPrecision();
            int p2 = type2.getPrecision();
            int s1 = type1.getScale();
            int s2 = type2.getScale();
            int intDig = p1 - s1 + s2;
            int scale = Math.max(MINIMUM_ADJUSTED_SCALE, s1 + p2 + 1);
            int precision = intDig + scale;
            RelDataType ret;
            int maxNumericPrecision = this.typeSystem.getMaxNumericPrecision();
            if (precision <= maxNumericPrecision) {
                return createSqlType(SqlTypeName.DECIMAL, precision, scale);
            } else {
                int intDigits = precision - scale;
                int minScaleValue = Math.min(scale, MINIMUM_ADJUSTED_SCALE);
                int adjustedScale = Math.max(maxNumericPrecision - intDigits, minScaleValue);
                ret = createSqlType(SqlTypeName.DECIMAL, maxNumericPrecision, adjustedScale);
            }
            return ret;
        }
        return null;
    }

    public KylinJavaTypeFactoryImpl(RelDataTypeSystem system) {
        super(system);
    }

}
