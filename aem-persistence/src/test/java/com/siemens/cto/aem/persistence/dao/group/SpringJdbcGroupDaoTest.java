package com.siemens.cto.aem.persistence.dao.group;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.h2.Driver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.ConfigurationProfile;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.audit.AuditDateTime;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.audit.AuditUser;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupEvent;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.group.UpdateGroupEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.group.impl.SpringJdbcGroupDao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {SpringJdbcGroupDaoTest.CommonConfiguration.class,
                                 SpringJdbcGroupDaoTest.IntegrationConfiguration.class,
                                 SpringJdbcGroupDaoTest.LocalDevConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class SpringJdbcGroupDaoTest  {

    @Configuration
    static class CommonConfiguration {

        @Autowired
        private DataSource transactionDataSource;

        @Bean(name = "transactionManager")
        public PlatformTransactionManager getTransactionManager() {
            return new DataSourceTransactionManager(transactionDataSource);
        }
    }

    @Configuration
    @Profile({ConfigurationProfile.LOCAL_DEVELOPMENT,
              ConfigurationProfile.DEFAULT})
    static class LocalDevConfiguration {

        @Bean
        public DataSource getDataSource() {
            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource(new Driver(),
                                                                                 "jdbc:h2:~/test",
                                                                                 "sa",
                                                                                 "");
            return dataSource;
        }
    }

    @Configuration
    @Profile(ConfigurationProfile.INTEGRATION)
    static class IntegrationConfiguration {

        @Bean
        public DataSource getDataSource() {
            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource(new Driver(),
                                                                                 "jdbc:h2:~/test",
                                                                                 "sa",
                                                                                 "");
            return dataSource;
        }
    }

    @Autowired
    private DataSource dataSource;

    private SpringJdbcGroupDao dao;

    @Before
    public void setUp() throws Exception {
        dao = new SpringJdbcGroupDao(dataSource);
    }

    @Test
    public void testCreateGroup() throws Exception {

        final CreateGroupEvent createGroupEvent = new CreateGroupEvent(new CreateGroupCommand("newGroupName"),
                                                                       new AuditEvent(new AuditUser("auditUserTest"),
                                                                                      new AuditDateTime(new Date())));

        final Group actualGroup = dao.createGroup(createGroupEvent);

        assertEquals(createGroupEvent.getCreateGroupCommand().getGroupName(),
                     actualGroup.getName());
        assertNotNull(actualGroup.getId());
    }

    @Test
    public void testUpdateGroup() throws Exception {

        final UpdateGroupEvent updateGroupEvent = new UpdateGroupEvent(new UpdateGroupCommand(new Identifier<Group>(338L),
                                                                                              "My New Name"),
                                                                       new AuditEvent(new AuditUser("auditUpdateUserTest"),
                                                                                      new AuditDateTime(new Date())));

        final Group actualGroup = dao.updateGroup(updateGroupEvent);

        assertEquals(updateGroupEvent.getUpdateGroupCommand().getNewName(),
                     actualGroup.getName());
        assertEquals(updateGroupEvent.getUpdateGroupCommand().getId(),
                     actualGroup.getId());
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> expectedGroupIdentifier = new Identifier<>(338L);

        final Group group = dao.getGroup(expectedGroupIdentifier);

        assertEquals("Test Group",
                     group.getName());
        assertEquals(expectedGroupIdentifier,
                     group.getId());
    }

    @Test
    public void testGetGroups() throws Exception {

        final PaginationParameter pagination = new PaginationParameter(0, 2);
        final List<Group> actualGroups = dao.getGroups(pagination);

        assertEquals(pagination.getLimit().intValue(),
                     actualGroups.size());
    }

    @Test
    public void testFindGroups() throws Exception {

        final String expectedContains = "Test";

        final List<Group> actualGroups = dao.findGroups(expectedContains,
                                                        new PaginationParameter());

        for(final Group group : actualGroups) {
            assertTrue(group.getName().contains(expectedContains));
        }
    }

    @Test(expected = DataAccessException.class)
    public void testRemoveGroup() throws Exception {

        final Identifier<Group> groupId = new Identifier<>(338L);

        dao.removeGroup(groupId);
        dao.getGroup(groupId);
    }
}
