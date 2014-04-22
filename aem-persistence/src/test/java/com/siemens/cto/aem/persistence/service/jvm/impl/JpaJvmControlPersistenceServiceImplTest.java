package com.siemens.cto.aem.persistence.service.jvm.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmControlCrudService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmControlCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {JpaJvmControlPersistenceServiceImplTest.Config.class
                      })
public class JpaJvmControlPersistenceServiceImplTest {

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public JvmControlPersistenceService getJvmControlPersistenceService() {
            return new JpaJvmControlPersistenceServiceImpl(getJvmControlCrudService());
        }

        @Bean
        public JvmControlCrudService getJvmControlCrudService() {
            return new JvmControlCrudServiceImpl();
        }
    }

    @Autowired
    private JvmControlPersistenceService persistenceService;

    @Test
    public void testAddNewControlOperation() {

        final Identifier<Jvm> jvmId = new Identifier<>(123456L);
        final JvmControlOperation operation = JvmControlOperation.START;
        final ControlJvmCommand command = new ControlJvmCommand(jvmId,
                                                                operation);
        final User user = new User("unused");
        final AuditEvent auditEvent = AuditEvent.now(user);
        final Event<ControlJvmCommand> event = new Event<>(command,
                                                           auditEvent);

        final JvmControlHistory history = persistenceService.addControlHistoryEvent(event);

        assertNotNull(history.getId());
        assertEquals(jvmId,
                     history.getJvmId());
        assertEquals(operation,
                     history.getControlOperation());
        assertEquals(user.getId(),
                     history.getWhenRequested().getUser().getUserId());
    }
}
