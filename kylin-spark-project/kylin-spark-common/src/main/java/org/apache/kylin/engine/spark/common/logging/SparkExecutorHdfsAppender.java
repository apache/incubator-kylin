package org.apache.kylin.engine.spark.common.logging;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.spark.SparkEnv;
import org.apache.spark.deploy.yarn.YarnSparkHadoopUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SparkExecutorHdfsAppender extends AbstractHdfsLogAppender {

    private static final long A_DAY_MILLIS = 24 * 60 * 60 * 1000L;
    private static final long A_HOUR_MILLIS = 60 * 60 * 1000L;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat hourFormat = new SimpleDateFormat("HH");

    @VisibleForTesting
    String outPutPath;
    @VisibleForTesting
    String executorId;

    @VisibleForTesting
    long startTime = 0;
    @VisibleForTesting
    boolean rollingByHour = false;
    @VisibleForTesting
    int rollingPeriod = 5;

    //log appender configurable
    private String metadataIdentifier;
    private String category;

    private String identifier;

    // only cubing job
    private String jobName;
    private String project;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setMetadataIdentifier(String metadataIdentifier) {
        this.metadataIdentifier = metadataIdentifier;
    }

    public String getMetadataIdentifier() {
        return metadataIdentifier;
    }

    @Override
    void init() {
        if (StringUtils.isBlank(this.identifier)) {
            this.identifier = YarnSparkHadoopUtil.getContainerId().getApplicationAttemptId().getApplicationId()
                    .toString();
        }

        LogLog.warn("metadataIdentifier -> " + getMetadataIdentifier());
        LogLog.warn("category -> " + getCategory());
        LogLog.warn("identifier -> " + getIdentifier());

        if (null != getProject()) {
            LogLog.warn("project -> " + getProject());
        }

        if (null != getJobName()) {
            LogLog.warn("jobName -> " + getJobName());
        }
    }

    @Override
    String getAppenderName() {
        return "SparkExecutorHdfsLogAppender";
    }

    @Override
    boolean isSkipCheckAndFlushLog() {
        if (SparkEnv.get() == null && StringUtils.isBlank(executorId)) {
            LogLog.warn("Waiting for spark executor to start");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LogLog.error("Waiting for spark executor starting is interrupted!", e);
                Thread.currentThread().interrupt();
            }
            return true;
        }
        return false;
    }

    @Override
    void doWriteLog(int size, List<LoggingEvent> transaction) throws IOException, InterruptedException {
        while (size > 0) {
            final LoggingEvent loggingEvent = getLogBufferQue().take();
            if (isTimeChanged(loggingEvent)) {
                updateOutPutDir(loggingEvent);

                final Path file = new Path(outPutPath);

                String sparkuser = System.getenv("SPARK_USER");
                String user = System.getenv("USER");
                LogLog.warn("login user is " + UserGroupInformation.getLoginUser() + " SPARK_USER is " + sparkuser
                        + " USER is " + user);
                if (!initHdfsWriter(file, new Configuration())) {
                    LogLog.error("Failed to init the hdfs writer!");
                }
                doRollingClean(loggingEvent);
            }

            transaction.add(loggingEvent);
            writeLogEvent(loggingEvent);
            size--;
        }
    }

    @VisibleForTesting
    void updateOutPutDir(LoggingEvent event) {
        if (rollingByHour) {
            String rollingDir = dateFormat.format(new Date(event.getTimeStamp())) + "/"
                    + hourFormat.format(new Date(event.getTimeStamp()));
            outPutPath = getOutPutDir(rollingDir);
        } else {
            String rollingDir = dateFormat.format(new Date(event.getTimeStamp()));
            outPutPath = getOutPutDir(rollingDir);
        }
    }

    private String getOutPutDir(String rollingDir) {
        if (StringUtils.isBlank(executorId)) {
            executorId = SparkEnv.get() != null ? SparkEnv.get().executorId() : UUID.randomUUID().toString();
            LogLog.warn("executorId set to " + executorId);
        }

        if ("job".equals(getCategory())) {
            return getRootPathName() + "/" + rollingDir + "/" + getIdentifier() + "/" + getJobName() + "/" + "executor-"
                    + executorId + ".log";
        }
        return getRootPathName() + "/" + rollingDir + "/" + getIdentifier() + "/" + "executor-" + executorId + ".log";
    }

    @VisibleForTesting
    void doRollingClean(LoggingEvent event) throws IOException {
        FileSystem fileSystem = getFileSystem();

        String rootPathName = getRootPathName();
        Path rootPath = new Path(rootPathName);

        if (!fileSystem.exists(rootPath))
            return;

        FileStatus[] logFolders = fileSystem.listStatus(rootPath);

        if (logFolders == null)
            return;

        String thresholdDay = dateFormat.format(new Date(event.getTimeStamp() - A_DAY_MILLIS * rollingPeriod));

        for (FileStatus fs : logFolders) {
            String fileName = fs.getPath().getName();
            if (fileName.compareTo(thresholdDay) < 0) {
                Path fullPath = new Path(rootPathName + File.separator + fileName);
                if (!fileSystem.exists(fullPath))
                    continue;
                fileSystem.delete(fullPath, true);
            }
        }
    }

    @VisibleForTesting
    String getRootPathName() {
        if ("job".equals(getCategory())) {
            return parseHdfsWordingDir() + "/" + getProject() + "/spark_logs";
        } else if ("sparder".equals(getCategory())) {
            return parseHdfsWordingDir() + "/_sparder_logs";
        } else {
            throw new IllegalArgumentException("illegal category: " + getCategory());
        }
    }

    @VisibleForTesting
    boolean isTimeChanged(LoggingEvent event) {
        if (rollingByHour) {
            return isNeedRolling(event, A_HOUR_MILLIS);
        } else {
            return isNeedRolling(event, A_DAY_MILLIS);
        }
    }

    private boolean isNeedRolling(LoggingEvent event, Long timeInterval) {
        if (0 == startTime || ((event.getTimeStamp() / timeInterval) - (startTime / timeInterval)) > 0) {
            startTime = event.getTimeStamp();
            return true;
        }
        return false;
    }

    private String parseHdfsWordingDir() {
        return StringUtils.appendIfMissing(getHdfsWorkingDir(), "/")
                + StringUtils.replace(getMetadataIdentifier(), "/", "-");
    }
}

