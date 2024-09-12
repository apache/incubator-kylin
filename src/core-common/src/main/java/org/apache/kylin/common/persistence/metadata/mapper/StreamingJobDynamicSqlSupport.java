package org.apache.kylin.common.persistence.metadata.mapper;

import org.mybatis.dynamic.sql.SqlColumn;

import java.sql.JDBCType;

public final class StreamingJobDynamicSqlSupport {

    public static final StreamingJob sqlTable = new StreamingJob();

    private StreamingJobDynamicSqlSupport() {
    }

    public static final class StreamingJob extends BasicSqlTable<StreamingJob> {

        public final SqlColumn<String> modelUuid = column("model_uuid", JDBCType.CHAR);

        public StreamingJob() {
            super("streaming_job", StreamingJob::new);
        }
    }
}
