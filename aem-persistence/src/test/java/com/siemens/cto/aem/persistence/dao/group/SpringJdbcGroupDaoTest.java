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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.ConfigurationProfile;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.exception.NotFoundException;
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
    private Group preCreatedGroup;

    @Before
    public void setUp() throws Exception {
        dao = new SpringJdbcGroupDao(dataSource);


        preCreatedGroup = dao.createGroup(createCreateGroupEvent("Pre-Created Group Name",
                                                                 "Pre-Created User Id"));
    }

    protected CreateGroupEvent createCreateGroupEvent(final String aGroupName,
                                                      final String aUserId) {

        final CreateGroupEvent createGroup = new CreateGroupEvent(new CreateGroupCommand(aGroupName),
                                                                  createAuditEvent(aUserId));

        return createGroup;
    }

    protected UpdateGroupEvent createUpdateGroupEvent(final Identifier<Group> aGroupId,
                                                      final String aNewGroupName,
                                                      final String aUserId) {

        final UpdateGroupEvent updateGroup = new UpdateGroupEvent(new UpdateGroupCommand(aGroupId,
                                                                                         aNewGroupName),
                                                                  createAuditEvent(aUserId));

        return updateGroup;
    }

    protected AuditEvent createAuditEvent(final String aUserId) {
        return new AuditEvent(new AuditUser(aUserId),
                              new AuditDateTime(new Date(System.currentTimeMillis())));
    }

    @Test
    public void testCreateGroup() throws Exception {

        final CreateGroupEvent createGroup = createCreateGroupEvent("newGroupName",
                                                                    "auditUserTest");

        final Group actualGroup = dao.createGroup(createGroup);

        assertEquals(createGroup.getCreateGroupCommand().getGroupName(),
                     actualGroup.getName());
        assertNotNull(actualGroup.getId());
    }

    @Test
    public void testUpdateGroup() throws Exception {

        final UpdateGroupEvent updateGroup = createUpdateGroupEvent(preCreatedGroup.getId(),
                                                                    "My New Name",
                                                                    "auditUpdateUserTest");

        final Group actualGroup = dao.updateGroup(updateGroup);

        assertEquals(updateGroup.getUpdateGroupCommand().getNewName(),
                     actualGroup.getName());
        assertEquals(updateGroup.getUpdateGroupCommand().getId(),
                     actualGroup.getId());
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> expectedGroupIdentifier = preCreatedGroup.getId();

        final Group group = dao.getGroup(expectedGroupIdentifier);

        assertEquals(preCreatedGroup.getName(),
                     group.getName());
        assertEquals(expectedGroupIdentifier,
                     group.getId());
    }

    @Test
    public void testGetGroups() throws Exception {

        final PaginationParameter pagination = new PaginationParameter(0, 2);

        for (int i=0; i<= pagination.getLimit(); i++) {
            dao.createGroup(createCreateGroupEvent("Auto-constructed Group " + (i + 1),
                                                   "Auto-constructed User " + (i + 1)));
        }

        final List<Group> actualGroups = dao.getGroups(pagination);

        assertEquals(pagination.getLimit().intValue(),
                     actualGroups.size());
    }

    @Test
    public void testFindGroups() throws Exception {

        final String expectedContains = preCreatedGroup.getName().substring(3, 5);

        final List<Group> actualGroups = dao.findGroups(expectedContains,
                                                        new PaginationParameter());

        for(final Group group : actualGroups) {
            assertTrue(group.getName().contains(expectedContains));
        }
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveGroup() throws Exception {

        final Identifier<Group> groupId = preCreatedGroup.getId();

        dao.removeGroup(groupId);
        dao.getGroup(groupId);
    }
}
