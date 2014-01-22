package com.siemens.cto.aem.service.configuration.application;

import junit.framework.TestCase;

public class ApacheEnterpriseManagerServiceAppConfigTest extends TestCase {

    public void testConstructor() {
        final ApacheEnterpriseManagerServiceAppConfig aemConfigConfig = new ApacheEnterpriseManagerServiceAppConfig();

        assertNotNull(aemConfigConfig);
    }
}
