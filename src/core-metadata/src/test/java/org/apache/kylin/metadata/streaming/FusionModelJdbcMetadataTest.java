package org.apache.kylin.metadata.streaming;

import org.apache.kylin.junit.annotation.JdbcMetadataInfo;
import org.apache.kylin.junit.annotation.MetadataInfo;
import org.apache.kylin.metadata.model.FusionModel;
import org.apache.kylin.metadata.model.FusionModelManager;
import org.apache.kylin.metadata.project.EnhancedUnitOfWork;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.apache.kylin.common.util.TestUtils.getTestConfig;

@MetadataInfo
@JdbcMetadataInfo
public class FusionModelJdbcMetadataTest {

    private static String PROJECT = "streaming_test";
    private static FusionModelManager mgr;

    @Test
    public void testNewFusionModel() {
        FusionModel copy = new FusionModel();
        EnhancedUnitOfWork.doInTransactionWithCheckAndRetry(() ->
                FusionModelManager.getInstance(getTestConfig(), PROJECT).createModel(copy), PROJECT);
        mgr = FusionModelManager.getInstance(getTestConfig(), PROJECT);
        Assertions.assertNotNull(mgr.getFusionModel(copy.getId()));
    }
}
