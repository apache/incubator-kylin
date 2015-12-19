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

package org.apache.kylin.job;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.kylin.cube.CubeSegment;
import org.apache.kylin.cube.model.CubeDesc;
import org.apache.kylin.job.engine.JobEngineConfig;
import org.apache.kylin.job.hadoop.hive.CubeJoinedFlatTableDesc;
import org.apache.kylin.job.hadoop.hive.IJoinedFlatTableDesc;
import org.apache.kylin.job.hadoop.hive.IntermediateColumnDesc;
import org.apache.kylin.job.hadoop.hive.SqlHiveDataTypeMapping;
import org.apache.kylin.metadata.model.DataModelDesc;
import org.apache.kylin.metadata.model.JoinDesc;
import org.apache.kylin.metadata.model.LookupDesc;
import org.apache.kylin.metadata.model.PartitionDesc;
import org.apache.kylin.metadata.model.TblColRef;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author George Song (ysong1)
 * 
 */

public class JoinedFlatTable {

    public static String generateCreateTableStatement(IJoinedFlatTableDesc intermediateTableDesc, String storageDfsDir, String jobUUID) {
        StringBuilder ddl = new StringBuilder();

        ddl.append("CREATE EXTERNAL TABLE IF NOT EXISTS " + intermediateTableDesc.getTableName(jobUUID) + "\n");

        ddl.append("(" + "\n");
        for (int i = 0; i < intermediateTableDesc.getColumnList().size(); i++) {
            IntermediateColumnDesc col = intermediateTableDesc.getColumnList().get(i);
            if (i > 0) {
                ddl.append(",");
            }
            ddl.append(colName(col.getCanonicalName()) + " " + SqlHiveDataTypeMapping.getHiveDataType(col.getDataType()) + "\n");
        }
        ddl.append(")" + "\n");

        ddl.append("ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\177'" + "\n");
        ddl.append("STORED AS SEQUENCEFILE" + "\n");
        ddl.append("LOCATION '" + storageDfsDir + "/" + intermediateTableDesc.getTableName(jobUUID) + "';").append("\n");
        // ddl.append("TBLPROPERTIES ('serialization.null.format'='\\\\N')" +
        // ";\n");
        return ddl.toString();
    }

    public static String generateDropTableStatement(IJoinedFlatTableDesc intermediateTableDesc, String jobUUID) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("DROP TABLE IF EXISTS " + intermediateTableDesc.getTableName(jobUUID) + ";").append("\n");
        return ddl.toString();
    }

    public static String generateInsertDataStatement(IJoinedFlatTableDesc intermediateTableDesc, String jobUUID, JobEngineConfig engineConfig, String projectName) throws IOException {
        StringBuilder sql = new StringBuilder();

        File hadoopPropertiesFile = new File(engineConfig.getHadoopJobConfFilePath(intermediateTableDesc.getCapacity(), projectName));

        if (hadoopPropertiesFile.exists()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            Document doc;
            try {
                builder = factory.newDocumentBuilder();
                doc = builder.parse(hadoopPropertiesFile);
                NodeList nl = doc.getElementsByTagName("property");
                for (int i = 0; i < nl.getLength(); i++) {
                    String name = doc.getElementsByTagName("name").item(i).getFirstChild().getNodeValue();
                    String value = doc.getElementsByTagName("value").item(i).getFirstChild().getNodeValue();
                    if (name.equals("tmpjars") == false) {
                        sql.append("SET " + name + "=" + value + ";").append("\n");
                    }
                }

            } catch (ParserConfigurationException e) {
                throw new IOException(e);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }

        // hard coded below mr parameters to enable map-side join
        sql.append("SET hive.exec.compress.output=true;").append("\n");
        sql.append("SET hive.auto.convert.join.noconditionaltask = true;").append("\n");
        sql.append("SET hive.auto.convert.join.noconditionaltask.size = 300000000;").append("\n");
        sql.append("INSERT OVERWRITE TABLE " + intermediateTableDesc.getTableName(jobUUID) + " " + generateSelectDataStatement(intermediateTableDesc) + ";").append("\n");

        return sql.toString();
    }


    public static String generateSelectDataStatement(IJoinedFlatTableDesc intermediateTableDesc) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT" + "\n");
        String tableAlias;
        Map<String, String> tableAliasMap = buildTableAliasMap(intermediateTableDesc.getDataModel());
        for (int i = 0; i < intermediateTableDesc.getColumnList().size(); i++) {
            IntermediateColumnDesc col = intermediateTableDesc.getColumnList().get(i);
            if (i > 0) {
                sql.append(",");
            }
            tableAlias = tableAliasMap.get(col.getTableName());
            sql.append(tableAlias + "." + col.getColumnName() + "\n");
        }
        appendJoinStatement(intermediateTableDesc, sql, tableAliasMap);
        appendWhereStatement(intermediateTableDesc, sql, tableAliasMap);
        return sql.toString();
    }

    private static Map<String, String> buildTableAliasMap(DataModelDesc dataModelDesc) {
        Map<String, String> tableAliasMap = new HashMap<String, String>();

        addTableAlias(dataModelDesc.getFactTable(), tableAliasMap);

        for (LookupDesc lookupDesc : dataModelDesc.getLookups()) {
            JoinDesc join = lookupDesc.getJoin();
            if (join != null) {
                addTableAlias(lookupDesc.getTable(), tableAliasMap);
            }
        }
        return tableAliasMap;
    }

    // The table alias used to be "FACT_TABLE" and "LOOKUP_#", but that's too unpredictable
    // for those who want to write a filter. (KYLIN-900)
    // Also yet don't support joining the same table more than once, since table name is the map key.
    private static void addTableAlias(String table, Map<String, String> tableAliasMap) {
        String alias;
        int cut = table.lastIndexOf('.');
        if (cut < 0)
            alias = table;
        else
            alias = table.substring(cut + 1);
        
        tableAliasMap.put(table, alias);
    }

    private static void appendJoinStatement(IJoinedFlatTableDesc intermediateTableDesc, StringBuilder sql, Map<String, String> tableAliasMap) {
        Set<String> dimTableCache = new HashSet<String>();

        DataModelDesc dataModelDesc = intermediateTableDesc.getDataModel();
        String factTableName = dataModelDesc.getFactTable();
        String factTableAlias = tableAliasMap.get(factTableName);
        sql.append("FROM " + factTableName + " as " + factTableAlias + " \n");

        for (LookupDesc lookupDesc : dataModelDesc.getLookups()) {
            JoinDesc join = lookupDesc.getJoin();
            if (join != null && join.getType().equals("") == false) {
                String joinType = join.getType().toUpperCase();
                String dimTableName = lookupDesc.getTable();
                if (!dimTableCache.contains(dimTableName)) {
                    TblColRef[] pk = join.getPrimaryKeyColumns();
                    TblColRef[] fk = join.getForeignKeyColumns();
                    if (pk.length != fk.length) {
                        throw new RuntimeException("Invalid join condition of lookup table:" + lookupDesc);
                    }
                    sql.append(joinType + " JOIN " + dimTableName + " as " + tableAliasMap.get(dimTableName) + "\n");
                    sql.append("ON ");
                    for (int i = 0; i < pk.length; i++) {
                        if (i > 0) {
                            sql.append(" AND ");
                        }
                        sql.append(factTableAlias + "." + fk[i].getName() + " = " + tableAliasMap.get(dimTableName) + "." + pk[i].getName());
                    }
                    sql.append("\n");

                    dimTableCache.add(dimTableName);
                }
            }
        }
    }

    private static void appendWhereStatement(IJoinedFlatTableDesc intermediateTableDesc, StringBuilder sql, Map<String, String> tableAliasMap) {
        if (!(intermediateTableDesc instanceof CubeJoinedFlatTableDesc)) {
            return;//TODO: for now only cube segments support filter and partition
        }
        CubeJoinedFlatTableDesc desc = (CubeJoinedFlatTableDesc) intermediateTableDesc;

        boolean hasCondition = false;
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append("WHERE");

        CubeDesc cubeDesc = desc.getCubeDesc();
        DataModelDesc model = cubeDesc.getModel();

        if (model.getFilterCondition() != null && model.getFilterCondition().equals("") == false) {
            whereBuilder.append(" (").append(model.getFilterCondition()).append(") ");
            hasCondition = true;
        }

        CubeSegment cubeSegment = desc.getCubeSegment();

        if (null != cubeSegment) {
            PartitionDesc partDesc = model.getPartitionDesc();
            long dateStart = cubeSegment.getDateRangeStart();
            long dateEnd = cubeSegment.getDateRangeEnd();

            if (!(dateStart == 0 && dateEnd == Long.MAX_VALUE)) {
                whereBuilder.append(hasCondition ? " AND (" : " (");
                whereBuilder.append(partDesc.getPartitionConditionBuilder().buildDateRangeCondition(partDesc, dateStart, dateEnd, tableAliasMap));
                whereBuilder.append(")\n");
                hasCondition = true;
            }
        }

        if (hasCondition) {
            sql.append(whereBuilder.toString());
        }
    }

    private static String colName(String canonicalColName) {
        return canonicalColName.replace(".", "_");
    }
}
