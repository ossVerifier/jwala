package com.siemens.cto.aem.service.state.impl;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl;
import com.siemens.cto.aem.service.state.StateNotificationGateway;


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { GroupStateServiceImplTest.CommonConfiguration.class })
public class GroupStateServiceImplTest {

    @Autowired
    @Qualifier("stateUpdates")
    SubscribableChannel stateUpdates; 
    
    @Autowired
    StateNotificationGateway stateNotificationGateway;
    
    @Autowired
    TaskExecutor notificationExecutor;

    @Autowired
    ServiceActivatingHandler  groupStateServiceActivator;
    
    @Autowired
    GroupStateServiceImpl   mockGroupStateService;

    int updateReceived = 0;
    
    @Test
    public void testSubscribeAndReceive() throws InterruptedException {
        stateUpdates.unsubscribe(groupStateServiceActivator);
        stateUpdates.subscribe(new MessageHandler() {
            
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                updateReceived = 1;
                synchronized(GroupStateServiceImplTest.this) {
                    GroupStateServiceImplTest.this.notify();
                }
            }
        });
        
        synchronized(this) {
            stateNotificationGateway.jvmStateChanged(new CurrentJvmState(id(0L, Jvm.class), JvmState.STARTED, DateTime.now()));
            this.wait(5000);
        }
        
        assertTrue(updateReceived > 0);
    }

    @Test
    public void testSubscribeAndReceiveIsParallel() throws InterruptedException {
        stateUpdates.unsubscribe(groupStateServiceActivator);
        stateUpdates.subscribe(new MessageHandler() {
            
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                try {
                    Thread.sleep(100);
                    synchronized(GroupStateServiceImplTest.this) {
                        ++updateReceived;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized(GroupStateServiceImplTest.this) {
                    GroupStateServiceImplTest.this.notify();
                }
            }
        });

        // test
        synchronized(this) {
            for(int i = 0; i< 5; ++i) {
                stateNotificationGateway.jvmStateChanged(new CurrentJvmState(id(0L, Jvm.class), JvmState.STARTED, DateTime.now()));
            }
            this.wait(250); // for the first one
        }

        Thread.sleep(100); // for the rest.
        
        assertEquals(5, updateReceived);
    }
        
    @Configuration
    @ImportResource("classpath*:META-INF/spring/integration-state.xml")
    static class CommonConfiguration {

        @Bean
        public GroupStateManagerTableImpl getGroupStateManagerTableImpl() {
            return Mockito.mock(GroupStateManagerTableImpl.class);
        }
        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return Mockito.mock(JvmPersistenceService.class);
        }
        @Bean
        public JvmStatePersistenceService getJvmStatePersistenceService() {
            return Mockito.mock(JvmStatePersistenceService.class);
        }
        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return Mockito.mock(GroupPersistenceService.class);
        }
    }
}
