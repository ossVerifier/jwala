package com.siemens.cto.aem.control;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;

public class IntegrationTestRule implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestRule.class);

    private final boolean shouldRunIntegrationTests;

    public IntegrationTestRule() {
        shouldRunIntegrationTests = System.getProperty(TestExecutionProfile.RUN_TEST_TYPES).contains(TestExecutionProfile.INTEGRATION);
    }

    public Statement apply(final Statement base,
                           final Description description) {
        if (shouldRunIntegrationTests) {
            return base;
        } else {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    LOGGER.info("Skipping test because it's an integration test and they have not been configured to run: {}", description);
                }
            };
        }
    }
}
