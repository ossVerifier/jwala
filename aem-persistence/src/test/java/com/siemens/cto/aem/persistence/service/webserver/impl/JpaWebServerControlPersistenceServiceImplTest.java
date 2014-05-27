package com.siemens.cto.aem.persistence.service.webserver.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.webserver.WebServerControlCrudService;
import com.siemens.cto.aem.persistence.jpa.service.webserver.impl.WebServerControlCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.webserver.WebServerControlPersistenceService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {JpaWebServerControlPersistenceServiceImplTest.Config.class
                      })
public class JpaWebServerControlPersistenceServiceImplTest {

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public WebServerControlPersistenceService getWebServerControlPersistenceService() {
            return new JpaWebServerControlPersistenceServiceImpl(getWebServerControlCrudService());
        }

        @Bean
        public WebServerControlCrudService getWebServerControlCrudService() {
            return new WebServerControlCrudServiceImpl();
        }
    }

    @Autowired
    private WebServerControlPersistenceService persistenceService;

    @Test
    public void testAddNewControlOperation() {

        final Identifier<WebServer> webServerId = new Identifier<>(123456L);
        final WebServerControlOperation operation = WebServerControlOperation.START;
        final ControlWebServerCommand command = new ControlWebServerCommand(webServerId,
                                                                operation);
        final User user = new User("unused");
        final AuditEvent auditEvent = AuditEvent.now(user);
        final Event<ControlWebServerCommand> event = new Event<>(command,
                                                           auditEvent);

        final WebServerControlHistory history = persistenceService.addIncompleteControlHistoryEvent(event);

        assertNotNull(history.getId());
        assertEquals(webServerId,
                     history.getWebServerId());
        assertEquals(operation,
                     history.getControlOperation());
        assertEquals(user.getId(),
                     history.getWhenRequested().getUser().getUserId());
        assertEquals(new ExecData(new ExecReturnCode(null),
                                  null,
                                  null),
                     history.getExecData());
    }

    @Test
    public void testCompleteControlOperation() {
        final Identifier<WebServer> webServerId = new Identifier<>(123456L);
        final WebServerControlOperation operation = WebServerControlOperation.START;
        final ControlWebServerCommand command = new ControlWebServerCommand(webServerId,
                                                                operation);
        final User user = new User("unused");
        final AuditEvent auditEvent = AuditEvent.now(user);
        final Event<ControlWebServerCommand> event = new Event<>(command,
                                                           auditEvent);

        final WebServerControlHistory history = persistenceService.addIncompleteControlHistoryEvent(event);

        final ExecData execData = new ExecData(new ExecReturnCode(0),
                                               "Completed successfully",
                                               "");
        final CompleteControlWebServerCommand completeCommand = new CompleteControlWebServerCommand(history.getId(),
                                                                                        execData);
        final Event<CompleteControlWebServerCommand> completeEvent = new Event<>(completeCommand,
                                                                           auditEvent);

        final WebServerControlHistory completedHistory = persistenceService.completeControlHistoryEvent(completeEvent);

        assertEquals(history.getId(),
                     completedHistory.getId());
        assertEquals(execData,
                     completedHistory.getExecData());
    }
}