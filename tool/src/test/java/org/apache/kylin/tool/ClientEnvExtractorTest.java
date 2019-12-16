package org.apache.kylin.tool;

import org.apache.kylin.common.util.HBaseMetadataTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class ClientEnvExtractorTest extends HBaseMetadataTestCase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() throws Exception {
        super.createTestMetadata();
    }

    @After
    public void after() throws Exception {
        super.cleanupTestMetadata();
    }

    @Test
    public void testNormal() throws IOException {
        File f = folder.newFolder("ClientEnvExtractorTest_testNormal");
        ClientEnvExtractor executor = new ClientEnvExtractor();
        executor.addShellOutput("pwd", f.getAbsolutePath(), "testNormal");
    }

    @Test(timeout = 5000)
    public void testTimeout() throws IOException {
        File f = folder.newFolder("ClientEnvExtractorTest_testTimeout");
        ClientEnvExtractor executor = new ClientEnvExtractor();
        executor.maxWaitSeconds = 2;
        executor.addShellOutput("sleep 1000", f.getAbsolutePath(), "testTimeout");
        executor.addShellOutput("pwd", f.getAbsolutePath(), "pwd");
    }

    @Test
    public void testError() throws IOException {
        File f = folder.newFolder("ClientEnvExtractorTest_testError");
        ClientEnvExtractor executor = new ClientEnvExtractor();
        executor.addShellOutput("CMD_NEVER_EXISTS", f.getAbsolutePath(), "testError");
    }
}