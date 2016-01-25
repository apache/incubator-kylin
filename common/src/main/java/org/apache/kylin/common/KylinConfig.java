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

package org.apache.kylin.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kylin.common.restclient.RestClient;
import org.apache.kylin.common.util.CliCommandExecutor;
import org.apache.kylin.common.util.Log4jConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * @author yangli9
 */
public class KylinConfig {
    
    /*
     * NO NEED TO DEFINE PUBLIC CONSTANTS FOR KEY NAMES!
     * 
     * Kylin configuration is exposed as public methods. A client never need to access key names directly.
     * Feel free to hard code key names, as long as they are encapsulated in this class. It reads better!
     */

    public static final String KYLIN_STORAGE_URL = "kylin.storage.url";

    public static final String KYLIN_ES_CLUSTER_URL = "kylin.es.cluster.url";

    public static final String KYLIN_METADATA_URL = "kylin.metadata.url";

    public static final String KYLIN_REST_SERVERS = "kylin.rest.servers";

    public static final String KYLIN_REST_TIMEZONE = "kylin.rest.timezone";

    public static final String KYLIN_JOB_CONCURRENT_MAX_LIMIT = "kylin.job.concurrent.max.limit";

    public static final String KYLIN_JOB_YARN_APP_REST_CHECK_URL = "kylin.job.yarn.app.rest.check.status.url";

    public static final String KYLIN_JOB_YARN_APP_REST_CHECK_INTERVAL_SECONDS = "kylin.job.yarn.app.rest.check.interval.seconds";

    public static final String KYLIN_TMP_HDFS_DIR = "kylin.tmp.hdfs.dir";

    public static final String HIVE_DATABASE_FOR_INTERMEDIATE_TABLE = "kylin.job.hive.database.for.intermediatetable";

    public static final String HIVE_TABLE_LOCATION_PREFIX = "hive.table.location.";

    public static final String KYLIN_JOB_REMOTE_CLI_PASSWORD = "kylin.job.remote.cli.password";

    public static final String KYLIN_JOB_REMOTE_CLI_USERNAME = "kylin.job.remote.cli.username";

    public static final String KYLIN_JOB_REMOTE_CLI_HOSTNAME = "kylin.job.remote.cli.hostname";

    public static final String KYLIN_JOB_REMOTE_CLI_WORKING_DIR = "kylin.job.remote.cli.working.dir";

    public static final String KYLIN_JOB_CMD_EXTRA_ARGS = "kylin.job.cmd.extra.args";
    /**
     * Toggle to indicate whether to use hive for table flattening. Default
     * true.
     */
    public static final String KYLIN_JOB_RUN_AS_REMOTE_CMD = "kylin.job.run.as.remote.cmd";

    public static final String KYLIN_JOB_MAPREDUCE_DEFAULT_REDUCE_COUNT_RATIO = "kylin.job.mapreduce.default.reduce.count.ratio";

    public static final String KYLIN_JOB_MAPREDUCE_DEFAULT_REDUCE_INPUT_MB = "kylin.job.mapreduce.default.reduce.input.mb";

    public static final String KYLIN_JOB_MAPREDUCE_MAX_REDUCER_NUMBER = "kylin.job.mapreduce.max.reducer.number";

    public static final String KYLIN_JOB_JAR = "kylin.job.jar";

    public static final String COPROCESSOR_LOCAL_JAR = "kylin.coprocessor.local.jar";

    public static final String KYLIN_JOB_LOG_DIR = "kylin.job.log.dir";

    public static final String KYLIN_HDFS_WORKING_DIR = "kylin.hdfs.working.dir";

    public static final String KYLIN_HBASE_CLUSTER_FS = "kylin.hbase.cluster.fs";

    /**
     * Kylin properties file
     */
    public static final String KYLIN_CONF_PROPERTIES_FILE = "kylin.properties";

    public static final String MAIL_ENABLED = "mail.enabled";

    public static final String MAIL_HOST = "mail.host";

    public static final String MAIL_USERNAME = "mail.username";

    public static final String MAIL_PASSWORD = "mail.password";

    public static final String MAIL_SENDER = "mail.sender";

    public static final String KYLIN_HOME = "KYLIN_HOME";

    public static final String KYLIN_CONF = "KYLIN_CONF";

    private static final Logger logger = LoggerFactory.getLogger(KylinConfig.class);

    public static final String VERSION = "${project.version}";

    public static final String HTABLE_DEFAULT_COMPRESSION_CODEC = "kylin.hbase.default.compression.codec";

    public static final String HBASE_REGION_CUT_SMALL = "kylin.hbase.region.cut.small";
    public static final String HBASE_REGION_CUT_MEDIUM = "kylin.hbase.region.cut.medium";
    public static final String HBASE_REGION_CUT_LARGE = "kylin.hbase.region.cut.large";

    public static final String HBASE_REGION_COUNT_MIN = "kylin.hbase.region.count.min";
    public static final String HBASE_REGION_COUNT_MAX = "kylin.hbase.region.count.max";

    // static cached instances
    private static KylinConfig ENV_INSTANCE = null;

    public static KylinConfig getInstanceFromEnv() {
        if (ENV_INSTANCE == null) {
            try {
                KylinConfig config = loadKylinConfig();
                ENV_INSTANCE = config;
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Failed to find KylinConfig ", e);
            }
        }
        return ENV_INSTANCE;
    }

    public static void destoryInstance() {
        ENV_INSTANCE = null;
    }

    public static enum UriType {
        PROPERTIES_FILE, REST_ADDR, LOCAL_FOLDER
    }

    private static UriType decideUriType(String metaUri) {

        try {
            File file = new File(metaUri);
            if (file.exists() || metaUri.contains("/")) {
                if (file.exists() == false) {
                    file.mkdirs();
                }
                if (file.isDirectory()) {
                    return UriType.LOCAL_FOLDER;
                } else if (file.isFile()) {
                    if (file.getName().equalsIgnoreCase(KYLIN_CONF_PROPERTIES_FILE)) {
                        return UriType.PROPERTIES_FILE;
                    } else {
                        throw new IllegalStateException("Metadata uri : " + metaUri + " is a local file but not kylin.properties");
                    }
                } else {
                    throw new IllegalStateException("Metadata uri : " + metaUri + " looks like a file but it's neither a file nor a directory");
                }
            } else {
                if (RestClient.matchFullRestPattern(metaUri))
                    return UriType.REST_ADDR;
                else
                    throw new IllegalStateException("Metadata uri : " + metaUri + " is not a valid REST URI address");
            }
        } catch (Exception e) {
            logger.info(e.getLocalizedMessage());
            throw new IllegalStateException("Metadata uri : " + metaUri + " is not recognized");
        }
    }

    public static KylinConfig createInstanceFromUri(String uri) {
        /**
         * --hbase: 1. PROPERTIES_FILE: path to kylin.properties 2. REST_ADDR:
         * rest service resource, format: user:password@host:port --local: 1.
         * LOCAL_FOLDER: path to resource folder
         */
        UriType uriType = decideUriType(uri);
        logger.info("The URI " + uri + " is recognized as " + uriType);

        if (uriType == UriType.LOCAL_FOLDER) {
            KylinConfig config = new KylinConfig();
            config.setMetadataUrl(uri);
            return config;
        } else if (uriType == UriType.PROPERTIES_FILE) {
            KylinConfig config;
            try {
                config = new KylinConfig();
                InputStream is = new FileInputStream(uri);
                config.reloadKylinConfig(is);
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return config;
        } else {// rest_addr
            try {
                KylinConfig config = new KylinConfig();
                RestClient client = new RestClient(uri);
                String propertyText = client.getKylinProperties();
                InputStream is = IOUtils.toInputStream(propertyText);
                config.reloadKylinConfig(is);
                is.close();
                return config;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static KylinConfig getKylinConfigFromInputStream(InputStream is) {
        KylinConfig config = new KylinConfig();
        config.reloadKylinConfig(is);
        return config;
    }

    // ============================================================================

    /**
     * Find config from environment. The Search process: 1. Check the
     * $KYLIN_CONF/kylin.properties 2. Check the $KYLIN_HOME/conf/kylin.properties
     *
     * @return
     */
    private static KylinConfig loadKylinConfig() {
        Log4jConfigurer.initLogger();

        InputStream is = getKylinPropertiesAsInputSteam();
        if (is == null) {
            throw new IllegalArgumentException("Failed to load kylin config");
        }
        KylinConfig config = new KylinConfig();
        config.reloadKylinConfig(is);

        return config;
    }

    private PropertiesConfiguration kylinConfig = new PropertiesConfiguration();

    private String metadataUrl;
    private String storageUrl;
    private String esClusterUrl;
    public CliCommandExecutor getCliCommandExecutor() throws IOException {
        CliCommandExecutor exec = new CliCommandExecutor();
        if (getRunAsRemoteCommand()) {
            exec.setRunAtRemote(getRemoteHadoopCliHostname(), getRemoteHadoopCliUsername(), getRemoteHadoopCliPassword());
        }
        return exec;
    }

    // ============================================================================

    public boolean isHiveReroutingEnabled() {
	return Boolean.parseBoolean(getOptional("kylin.route.hive.enabled", "false")); 
    }

    public String getHiveRerouteUrl() {
        return getOptional("kylin.route.hive.url", "jdbc:hive2://");
    }

    public String getHiveRerouteUsername() {
        return getOptional("kylin.route.hive.username", "");
    }

    public String getHiveReroutePassword() {
        return getOptional("kylin.route.hive.password", "");
    }

    public String getStorageUrl() {
        return storageUrl;
    }
    public String getKylinEsClusterUrl(){
        return esClusterUrl;
    }
    /** Use the hive reroute feature instead */
    @Deprecated
    public String getHiveUrl() {
        return getOptional("hive.url", "");
    }

    /** Use the hive reroute feature instead */
    @Deprecated
    public String getHiveUser() {
        return getOptional("hive.user", "");
    }

    /** Use the hive reroute feature instead */
    @Deprecated
    public String getHivePassword() {
        return getOptional("hive.password", "");
    }

    public String getHdfsWorkingDirectory() {
        String root = getRequired(KYLIN_HDFS_WORKING_DIR);
        if (!root.endsWith("/")) {
            root += "/";
        }
        return root + getMetadataUrlPrefix() + "/";
    }

    public String getHBaseClusterFs() {
        return getOptional(KYLIN_HBASE_CLUSTER_FS, "");
    }

    public String getKylinJobLogDir() {
        return getOptional(KYLIN_JOB_LOG_DIR, "/tmp/kylin/logs");
    }

    public String getKylinJobJarPath() {
        final String jobJar = getOptional(KYLIN_JOB_JAR);
        if (StringUtils.isNotEmpty(jobJar)) {
            return jobJar;
        }
        String kylinHome = getKylinHome();
        if (StringUtils.isEmpty(kylinHome)) {
            return "";
        }
        return getFileName(kylinHome + File.separator + "lib", JOB_JAR_NAME_PATTERN);
    }
    
    public String getKylinJobMRLibDir() {
        return getOptional("kylin.job.mr.lib.dir", "");
    }

    public void overrideKylinJobJarPath(String path) {
        logger.info("override " + KYLIN_JOB_JAR + " to " + path);
        System.setProperty(KYLIN_JOB_JAR, path);
    }

    private static final Pattern COPROCESSOR_JAR_NAME_PATTERN = Pattern.compile("kylin-coprocessor-(.+)\\.jar");
    private static final Pattern JOB_JAR_NAME_PATTERN = Pattern.compile("kylin-job-(.+)\\.jar");

    public String getCoprocessorLocalJar() {
        final String coprocessorJar = getOptional(COPROCESSOR_LOCAL_JAR);
        if (StringUtils.isNotEmpty(coprocessorJar)) {
            return coprocessorJar;
        }
        String kylinHome = getKylinHome();
        if (StringUtils.isEmpty(kylinHome)) {
            throw new RuntimeException("getCoprocessorLocalJar needs KYLIN_HOME");
        }
        return getFileName(kylinHome + File.separator + "lib", COPROCESSOR_JAR_NAME_PATTERN);
    }

    private static String getFileName(String homePath, Pattern pattern) {
        File home = new File(homePath);
        SortedSet<String> files = Sets.newTreeSet();
        if (home.exists() && home.isDirectory()) {
            for (File file : home.listFiles()) {
                final Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    files.add(file.getAbsolutePath());
                }
            }
        }
        if (files.isEmpty()) {
            throw new RuntimeException("cannot find " + pattern.toString() + " in " + homePath);
        } else {
            return files.last();
        }
    }

    public void overrideCoprocessorLocalJar(String path) {
        logger.info("override " + COPROCESSOR_LOCAL_JAR + " to " + path);
        System.setProperty(COPROCESSOR_LOCAL_JAR, path);
    }

    public double getDefaultHadoopJobReducerInputMB() {
        return Double.parseDouble(getOptional(KYLIN_JOB_MAPREDUCE_DEFAULT_REDUCE_INPUT_MB, "500"));
    }

    public double getDefaultHadoopJobReducerCountRatio() {
        return Double.parseDouble(getOptional(KYLIN_JOB_MAPREDUCE_DEFAULT_REDUCE_COUNT_RATIO, "1.0"));
    }

    public int getHadoopJobMaxReducerNumber() {
        return Integer.parseInt(getOptional(KYLIN_JOB_MAPREDUCE_MAX_REDUCER_NUMBER, "5000"));
    }

    public boolean getRunAsRemoteCommand() {
        return Boolean.parseBoolean(getOptional(KYLIN_JOB_RUN_AS_REMOTE_CMD));
    }

    public String getRemoteHadoopCliHostname() {
        return getOptional(KYLIN_JOB_REMOTE_CLI_HOSTNAME);
    }

    public String getRemoteHadoopCliUsername() {
        return getOptional(KYLIN_JOB_REMOTE_CLI_USERNAME);
    }

    public String getRemoteHadoopCliPassword() {
        return getOptional(KYLIN_JOB_REMOTE_CLI_PASSWORD);
    }

    public String getCliWorkingDir() {
        return getOptional(KYLIN_JOB_REMOTE_CLI_WORKING_DIR);
    }

    public String getMapReduceCmdExtraArgs() {
        return getOptional(KYLIN_JOB_CMD_EXTRA_ARGS);
    }

    public String getOverrideHiveTableLocation(String table) {
        return getOptional(HIVE_TABLE_LOCATION_PREFIX + table.toUpperCase());
    }

    public String getTempHDFSDir() {
        return getOptional(KYLIN_TMP_HDFS_DIR, "/tmp/kylin");
    }

    public String getYarnStatusCheckUrl() {
        return getOptional(KYLIN_JOB_YARN_APP_REST_CHECK_URL, null);
    }

    public int getYarnStatusCheckIntervalSeconds() {
        return Integer.parseInt(getOptional(KYLIN_JOB_YARN_APP_REST_CHECK_INTERVAL_SECONDS, "60"));
    }

    public int getMaxConcurrentJobLimit() {
        return Integer.parseInt(getOptional(KYLIN_JOB_CONCURRENT_MAX_LIMIT, "10"));
    }

    public String getTimeZone() {
        return getOptional(KYLIN_REST_TIMEZONE, "PST");
    }

    public String[] getRestServers() {
        return getOptionalStringArray(KYLIN_REST_SERVERS);
    }

    public String getAdminDls() {
        return getOptional("kylin.job.admin.dls", null);
    }

    public long getJobStepTimeout() {
        return Long.parseLong(getOptional("kylin.job.step.timeout", String.valueOf(2 * 60 * 60)));
    }

    public String getServerMode() {
        return this.getOptional("kylin.server.mode", "all");
    }

    public int getDictionaryMaxCardinality() {
        return Integer.parseInt(getOptional("kylin.dictionary.max.cardinality", "5000000"));
    }

    public int getTableSnapshotMaxMB() {
        return Integer.parseInt(getOptional("kylin.table.snapshot.max_mb", "300"));
    }

    public int getScanThreshold() {
        return Integer.parseInt(getOptional("kylin.query.scan.threshold", "10000000"));
    }

    public boolean getQueryRunLocalCoprocessor() {
        return Boolean.parseBoolean(getOptional("kylin.query.run.local.coprocessor", "false"));
    }

    public Long getQueryDurationCacheThreshold() {
        return Long.parseLong(this.getOptional("kylin.query.cache.threshold.duration", String.valueOf(2000)));
    }

    public Long getQueryScanCountCacheThreshold() {
        return Long.parseLong(this.getOptional("kylin.query.cache.threshold.scancount", String.valueOf(10 * 1024)));
    }

    public boolean isQuerySecureEnabled() {
        return Boolean.parseBoolean(this.getOptional("kylin.query.security.enabled", "false"));
    }

    public boolean isQueryCacheEnabled() {
        return Boolean.parseBoolean(this.getOptional("kylin.query.cache.enabled", "true"));
    }

    public long getQueryMemBudget() {
        return Long.parseLong(this.getOptional("kylin.query.mem.budget", String.valueOf(3L * 1024 * 1024 * 1024)));
    }

    public int getHBaseKeyValueSize() {
        return Integer.parseInt(this.getOptional("kylin.hbase.client.keyvalue.maxsize", "10485760"));
    }
    
    public int getHBaseScanCacheRows() {
        return Integer.parseInt(this.getOptional("kylin.hbase.scan.cache_rows", "1024"));
    }

    public int getHBaseScanMaxResultSize() {
        return Integer.parseInt(this.getOptional("kylin.hbase.scan.max_result_size", "" + (5 * 1024 * 1024))); // 5 MB
    }
    
    public String getHbaseDefaultCompressionCodec() {
        return getOptional(HTABLE_DEFAULT_COMPRESSION_CODEC, "");

    }

    private String getOptional(String prop) {
        final String property = System.getProperty(prop);
        return property != null ? property : kylinConfig.getString(prop);
    }

    private String[] getOptionalStringArray(String prop) {
        final String property = System.getProperty(prop);
        if (!StringUtils.isBlank(property))
            return property.split("\\s*,\\s*");

        return kylinConfig.getStringArray(prop);
    }

    private String getOptional(String prop, String dft) {
        final String property = System.getProperty(prop);
        return property != null ? property : kylinConfig.getString(prop, dft);
    }

    private String getRequired(String prop) {
        final String property = System.getProperty(prop);
        if (property != null) {
            return property;
        }
        String r = kylinConfig.getString(prop);
        if (StringUtils.isEmpty(r)) {
            throw new IllegalArgumentException("missing '" + prop + "' in conf/kylin_instance.properties");
        }
        return r;
    }

    void reloadKylinConfig(InputStream is) {
        PropertiesConfiguration config = new PropertiesConfiguration();
        try {
            config.load(is);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Cannot load kylin config.", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.error("Failed to close inputstream.", e);
            }
        }
        this.kylinConfig = config;
        this.metadataUrl = getOptional(KYLIN_METADATA_URL);
        this.storageUrl = getOptional(KYLIN_STORAGE_URL);
        this.esClusterUrl = getOptional(KYLIN_ES_CLUSTER_URL);
    }

    public void writeProperties(File file) throws IOException {
        try {
            kylinConfig.save(file);
        } catch (ConfigurationException ex) {
            throw new IOException("Error writing KylinConfig to " + file, ex);
        }
    }

    public static String getKylinHome() {
        String kylinHome = System.getenv(KYLIN_HOME);
        if (StringUtils.isEmpty(kylinHome)) {
            logger.warn("KYLIN_HOME was not set");
            return kylinHome;
        }
        return kylinHome;
    }

    public void printProperties() throws IOException {
        try {
            kylinConfig.save(System.out);
        } catch (ConfigurationException ex) {
            throw new IOException("Error printing KylinConfig", ex);
        }
    }

    private static File getKylinProperties() {
        String kylinConfHome = System.getProperty(KYLIN_CONF);
        if (!StringUtils.isEmpty(kylinConfHome)) {
            logger.info("Use KYLIN_CONF=" + kylinConfHome);
            return getKylinPropertiesFile(kylinConfHome);
        }

        logger.warn("KYLIN_CONF property was not set, will seek KYLIN_HOME env variable");

        String kylinHome = getKylinHome();
        if (StringUtils.isEmpty(kylinHome))
            throw new RuntimeException("Didn't find KYLIN_CONF or KYLIN_HOME, please set one of them");

        String path = kylinHome + File.separator + "conf";
        return getKylinPropertiesFile(path);

    }

    public static InputStream getKylinPropertiesAsInputSteam() {
        File propFile = getKylinProperties();
        if (propFile == null || !propFile.exists()) {
            logger.error("fail to locate kylin.properties");
            throw new RuntimeException("fail to locate kylin.properties");
        }

        File overrideFile = new File(propFile.getParentFile(), propFile.getName() + ".override");
        if (overrideFile.exists()) {
            try {
                PropertiesConfiguration conf = new PropertiesConfiguration();
                conf.load(propFile);
                PropertiesConfiguration override = new PropertiesConfiguration();
                override.load(overrideFile);
                conf.copy(override);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                conf.save(bout);
                return new ByteArrayInputStream(bout.toByteArray());
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return new FileInputStream(propFile);
        } catch (FileNotFoundException e) {
            logger.error("this should not happen");
            throw new RuntimeException(e);
        }

    }

    /**
     * Check if there is kylin.properties exist
     *
     * @param path
     * @return the properties file
     */
    private static File getKylinPropertiesFile(String path) {
        if (path == null) {
            return null;
        }

        return new File(path, KYLIN_CONF_PROPERTIES_FILE);
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public String getMetadataUrlPrefix() {
        String hbaseMetadataUrl = getMetadataUrl();
        String defaultPrefix = "kylin_metadata";

        if (hbaseMetadataUrl.indexOf("@hbase") > 0) {
            int cut = hbaseMetadataUrl.indexOf('@');
            String tmp = cut < 0 ? defaultPrefix : hbaseMetadataUrl.substring(0, cut);
            return tmp;
        } else {
            return defaultPrefix;
        }
    }

    public void setMetadataUrl(String metadataUrl) {
        kylinConfig.setProperty(KYLIN_METADATA_URL, metadataUrl);
        this.metadataUrl = metadataUrl;
    }

    public void setStorageUrl(String storageUrl) {
        kylinConfig.setProperty(KYLIN_STORAGE_URL, storageUrl);
        this.storageUrl = storageUrl;
    }
    public void setKylinEsClusterUrl(String esClusterUrl) {
        kylinConfig.setProperty(KYLIN_ES_CLUSTER_URL, esClusterUrl);
        this.esClusterUrl = esClusterUrl;
    }
    public String getHiveDatabaseForIntermediateTable() {
        return this.getOptional(HIVE_DATABASE_FOR_INTERMEDIATE_TABLE, "default");
    }

    public void setRunAsRemoteCommand(String v) {
        kylinConfig.setProperty(KYLIN_JOB_RUN_AS_REMOTE_CMD, v);
    }

    public void setRemoteHadoopCliHostname(String v) {
        kylinConfig.setProperty(KYLIN_JOB_REMOTE_CLI_HOSTNAME, v);
    }

    public void setRemoteHadoopCliUsername(String v) {
        kylinConfig.setProperty(KYLIN_JOB_REMOTE_CLI_USERNAME, v);
    }

    public void setRemoteHadoopCliPassword(String v) {
        kylinConfig.setProperty(KYLIN_JOB_REMOTE_CLI_PASSWORD, v);
    }

    public int getHBaseRegionCountMin() {
        return Integer.parseInt(getOptional(HBASE_REGION_COUNT_MIN, "1"));
    }

    public int getHBaseRegionCountMax() {
        return Integer.parseInt(getOptional(HBASE_REGION_COUNT_MAX, "500"));
    }
    
    public int getHBaseRegionCut(String capacity) {
        String cut;
        switch (capacity) {
            case "SMALL":
                cut = getProperty(HBASE_REGION_CUT_SMALL, "10");
                break;
            case "MEDIUM":
                cut = getProperty(HBASE_REGION_CUT_MEDIUM, "20");
                break;
            case "LARGE":
                cut = getProperty(HBASE_REGION_CUT_LARGE, "100");
                break;
            default:
                throw new IllegalArgumentException("Capacity not recognized: " + capacity);
        }

        return Integer.valueOf(cut);
    }
    
    public String getProperty(String key, String defaultValue) {
        return kylinConfig.getString(key, defaultValue);
    }

    /**
     * Set a new key:value into the kylin config.
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        logger.info("Kylin Config was updated with " + key + " : " + value);
        kylinConfig.setProperty(key, value);
    }

    public String getConfigAsString() throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            kylinConfig.save(baos);
            String content = baos.toString();
            return content;
        } catch (ConfigurationException ex) {
            throw new IOException("Error writing KylinConfig to String", ex);
        }
    }

    public String toString() {
        return getMetadataUrl();
    }

}
