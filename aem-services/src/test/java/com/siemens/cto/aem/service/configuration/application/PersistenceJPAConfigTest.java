package com.siemens.cto.aem.service.configuration.application;

import junit.framework.TestCase;

public class PersistenceJPAConfigTest extends TestCase {

    public void testConstructor() {
        final PersistenceJPAConfig persistenceJPAConfig = new PersistenceJPAConfig();

        assertNotNull(persistenceJPAConfig);
    }

}
