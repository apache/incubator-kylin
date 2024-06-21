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

package org.apache.kylin.job.config;

import static org.apache.kylin.common.persistence.metadata.jdbc.JdbcUtil.isTableExists;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.persistence.metadata.JdbcDataSource;
import org.apache.kylin.common.persistence.metadata.jdbc.JdbcUtil;
import org.apache.kylin.job.condition.JobModeCondition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.util.InMemoryResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import lombok.val;
import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Conditional(JobModeCondition.class)
public class JobMybatisConfig implements InitializingBean {

    private DataSource dataSource;

    private String database;

    public static String JOB_INFO_TABLE = "job_info";
    public static String JOB_LOCK_TABLE = "job_lock";

    public String getDatabase() {
        return database;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setupJobTables();
    }

    public void setupJobTables() throws Exception {
        val url = KylinConfig.getInstanceFromEnv().getMetadataUrl();
        val props = JdbcUtil.datasourceParameters(url);
        dataSource = JdbcDataSource.getDataSource(props);

        String keIdentified = url.getIdentifier();
        if (StringUtils.isEmpty(url.getScheme())){
            log.info("metadata from file");
            keIdentified = "file";
        }
        JOB_INFO_TABLE = keIdentified + "_job_info";
        JOB_LOCK_TABLE = keIdentified + "_job_lock";
        database = Database.MYSQL.databaseId;

        String jobInfoFile = "script/schema_job_info_mysql.sql";
        String jobLockFile = "script/schema_job_lock_mysql.sql";

        Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(dataSource.getClass());
        List<Method> getDriverClassNameMethodList = Arrays.stream(declaredMethods)
                .filter(method -> "getDriverClassName".equals(method.getName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(getDriverClassNameMethodList)) {
            log.warn("can not get method of [getDriverClassName] from datasource type = {}", dataSource.getClass());
        } else {
            Method methodOfgetDriverClassName = getDriverClassNameMethodList.get(0);
            String driverClassName = (String) ReflectionUtils.invokeMethod(methodOfgetDriverClassName, dataSource);
            if (driverClassName.startsWith("com.mysql")) {
                // mysql, is default
                log.info("driver class name = {}, is mysql", driverClassName);
            } else if (driverClassName.startsWith("org.postgresql")) {
                log.info("driver class name = {}, is postgresql", driverClassName);
                jobInfoFile = "script/schema_job_info_postgresql.sql";
                jobLockFile = "script/schema_job_lock_postgresql.sql";
                database = Database.POSTGRESQL.databaseId;
            } else if (driverClassName.equals("org.h2.Driver")) {
                log.info("driver class name = {}, is H2 ", driverClassName);
                jobInfoFile = "script/schema_job_info_h2.sql";
                jobLockFile = "script/schema_job_lock_h2.sql";
                database = Database.H2.databaseId;
            } else {
                String errorMsg = String.format("driver class name = %s, should add support", driverClassName);
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
        try {
            if (!isTableExists(dataSource.getConnection(), JOB_INFO_TABLE)) {
                createTableIfNotExist(keIdentified, jobInfoFile);
            }

            if (!isTableExists(dataSource.getConnection(), JOB_LOCK_TABLE)) {
                createTableIfNotExist(keIdentified, jobLockFile);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTableIfNotExist(String keIdentified, String sql) throws IOException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

        var sessionScript = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(sql),
                StandardCharsets.UTF_8);
        sessionScript = sessionScript.replaceAll("KE_IDENTIFIED", keIdentified);
        populator.addScript(new InMemoryResource(sessionScript));
        populator.setContinueOnError(false);
        DatabasePopulatorUtils.execute(populator, dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    enum Database {
        MYSQL("mysql"),
        POSTGRESQL("postgresql"),
        H2("h2");

        String databaseId;

        Database(String databaseId) {
            this.databaseId = databaseId;
        }


    }
}
