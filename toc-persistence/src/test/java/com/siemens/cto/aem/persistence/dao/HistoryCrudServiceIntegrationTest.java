package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.HistoryCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Integration test for {@link HistoryCrudService}.
 *
 * Created by JC043760 on 11/30/2015.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {HistoryCrudServiceIntegrationTest.Config.class, TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class HistoryCrudServiceIntegrationTest {

    @Configuration
    static class Config {

        @Bean
        GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        HistoryCrudService getHistoryCrudService() {
            return new HistoryCrudServiceImpl();
        }

    }

    @Autowired
    private GroupCrudService groupCrudService;

    @Autowired
    private HistoryCrudService historyCrudService;

    @Test
    public void testWriteAndReadHistory() {
        final String SERVER_NAME = "zServerName";
        final JpaGroup jpaGroup = new JpaGroup();
        jpaGroup.setName("zGroup");
        groupCrudService.create(jpaGroup);
        historyCrudService.createHistory(SERVER_NAME, new Group(new Identifier<Group>(jpaGroup.getId()), "zGroup"), "Testing...", EventType.USER_ACTION, "any");

        List<JpaHistory> jpaHistoryList = historyCrudService.findHistory(jpaGroup.getName(), SERVER_NAME, 1);
        assertEquals(1, jpaHistoryList.size());
        assertEquals(EventType.USER_ACTION, jpaHistoryList.get(0).getEventType());

        jpaHistoryList = historyCrudService.findHistory(jpaGroup.getName(), SERVER_NAME, null);
        assertEquals(1, jpaHistoryList.size());

        jpaHistoryList = historyCrudService.findHistory(jpaGroup.getName(), null, null);
        assertEquals(1, jpaHistoryList.size());
    }

}
