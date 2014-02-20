package com.siemens.cto.aem.service;

import com.siemens.cto.aem.common.User;
import com.siemens.cto.aem.persistence.domain.Jvm;
import com.siemens.cto.aem.service.configuration.application.ApacheEnterpriseManagerServiceAppConfig;
import com.siemens.cto.aem.service.configuration.application.DaoConfig;
import com.siemens.cto.aem.service.configuration.application.PersistenceJPAConfig;
import com.siemens.cto.aem.service.configuration.application.PropertiesConfig;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class JvmInfoServiceIT extends TestCase {

    private ApplicationContext context;
    private JvmInfoService jvmInfoService;
    private static final String JVM_01 = "JulietteVictoryMama_ZeroOne";

    @Override
    protected void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext(PropertiesConfig.class,
                                                         PersistenceJPAConfig.class,
                                                         DaoConfig.class,
                                                         ApacheEnterpriseManagerServiceAppConfig.class);
        jvmInfoService = (JvmInfoService) context.getBean("jvmInfoService");

        // Required by com.siemens.cto.aem.persistence.domain.AbstractEntity
        final User user = new User("testUser", "password");
        user.addToThread();
    }

    /**
     * This test performs a "crude" CRUD integration test - get it ? :)
     * This test is inherently "flawed" since it might
     * fail if the database is dirty.
     *
     * TODO Module/pre-test init that makes sure that the database is clean.
     */
    public void testJvmCrud() {
        jvmInfoService.addJvmInfo("Test", "");
        List<Jvm> jvms = jvmInfoService.getAllJvmInfo();
        assertTrue(jvms.size() > 0);
        assertFalse(jvms.get(0).getName().equalsIgnoreCase(JVM_01));
        jvmInfoService.updateJvmInfo(jvms.get(0).getId(), JVM_01, "");
        Jvm jvm = jvmInfoService.getJvmInfoById(jvms.get(0).getId());
        assertTrue(jvm.getName().equalsIgnoreCase(JVM_01));
        jvmInfoService.deleteJvm(jvm.getId());
        assertEquals(0, jvmInfoService.getAllJvmInfo().size());
    }

}
