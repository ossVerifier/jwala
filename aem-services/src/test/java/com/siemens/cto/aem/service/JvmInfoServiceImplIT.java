package com.siemens.cto.aem.service;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.persistence.dao.GroupDaoJpa;
import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                        classes = {TestJpaConfiguration.class,
                        JvmInfoServiceImplIT.Config.class,
                        JvmInfoServiceImplIT.DaoConfig.class})
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
public class JvmInfoServiceImplIT {

    @Configuration
    static class Config {

        @Autowired
        private com.siemens.cto.aem.persistence.dao.GroupDaoJpa groupDao;

        @Autowired
        private com.siemens.cto.aem.persistence.dao.JvmDaoJpa jvmDao;

        @Bean
        public JvmInfoService getJvmInfoService() {
            return new JvmInfoServiceImpl(groupDao,
                                          jvmDao);
        }
    }

    @Configuration
    static class DaoConfig {

        @Bean
        public GroupDaoJpa getGroupDao() {
            return new GroupDaoJpa();
        }

        @Bean
        public JvmDaoJpa getJvmDao() {
            return new JvmDaoJpa();
        }

    }

    @Autowired
    private JvmInfoService jvmInfoService;

    private static final String JVM_01 = "JulietteVictoryMama_ZeroOne";

    /**
     * This test performs a "crude" CRUD integration test - get it ? :)
     * This test is inherently "flawed" since it might
     * fail if the database is dirty.
     *
     * TODO Module/pre-test init that makes sure that the database is clean.
     */
    @Test
    @Ignore
    public void testJvmCrud() {
        jvmInfoService.addJvmInfo("Test", "", new GroupInfo("Test Group"));
        List<JvmInfo> jvmInfoList = jvmInfoService.getAllJvmInfo();
        assertTrue(jvmInfoList.size() > 0);
        assertFalse(jvmInfoList.get(0).getName().equalsIgnoreCase(JVM_01));
        jvmInfoService.updateJvmInfo(jvmInfoList.get(0).getId(), JVM_01, "");
        JvmInfo jvmInfo = jvmInfoService.getJvmInfoById(jvmInfoList.get(0).getId());
        assertTrue(jvmInfo.getName().equalsIgnoreCase(JVM_01));
        assertEquals("Test Group", jvmInfoService.getAllJvmInfo().get(0).getGroupInfo().getName());
        jvmInfoService.deleteJvm(jvmInfo.getId());
        assertEquals(0, jvmInfoService.getAllJvmInfo().size());
        // TODO: Delete group!
    }

    @Test
    public void testCreateJvm() {

        final GroupInfo newGroup = new GroupInfo("Test Group");
        final String newJvmName = "Test";
        final String newHostName = "";

        jvmInfoService.addJvmInfo(newJvmName,
                                  newHostName,
                                  newGroup);

        final JvmInfo actualJvm = jvmInfoService.getJvmInfoByName(newJvmName);
        assertEquals(newJvmName,
                     actualJvm.getName());
    }

}