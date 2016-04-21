package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.common.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.common.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.common.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.common.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.common.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { DispatchRequestIntegrationTest.CommonConfiguration.class })
public class DispatchRequestIntegrationTest {

    private static final Identifier<Jvm> JVM1_IDENTIFIER = new Identifier<>((long) 1);
    private static final Identifier<Jvm> JVM2_IDENTIFIER = new Identifier<>((long) 2);
    private static final Identifier<WebServer> WS1_IDENTIFIER = new Identifier<>((long) 11);
    private static final Identifier<WebServer> WS2_IDENTIFIER = new Identifier<>((long) 12);
    private static final Identifier<Group> GROUP1_IDENTIFIER = new Identifier<>((long) 11);
    private BlockingQueue<Message<?>> blockingQueue;

    @Autowired
    private CommandDispatchGateway gateway;

    @Autowired
    @Qualifier("jvmAggregated")
    private DirectChannel jvmCommandCompletionChannel;

    @Autowired
    @Qualifier("webServerAggregated")
    private DirectChannel wsCommandCompletionChannel;

    private Jvm mockJvm1;
    private Jvm mockJvm2;
    private WebServer mockWs1;
    private WebServer mockWs2;

    // Using static variables in unit tests that are initialized in the class level is not good practice.
    // TODO: Refactor this in the near future.
    private static List<Jvm> jvmList = new ArrayList<>();
    private static List<WebServer> wsList = new ArrayList<>();

    private Group theGroup;

    @Before
    public void setup() {
        mockJvm1 = mock(Jvm.class);
        when(mockJvm1.getId()).thenReturn(JVM1_IDENTIFIER);
        mockJvm2 = mock(Jvm.class);
        when(mockJvm2.getId()).thenReturn(JVM2_IDENTIFIER);
        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mockJvm1);
        jvmSet.add(mockJvm2);
        theGroup = new Group(GROUP1_IDENTIFIER, "group1", jvmSet);
        blockingQueue = new ArrayBlockingQueue<>(1);
    }

    @Test
    public void splitGroupIntoTwoJvmControls() throws InterruptedException {
        jvmList.add(mockJvm1);
        jvmList.add(mockJvm2);

        ControlGroupJvmRequest startGroupCommand = new ControlGroupJvmRequest(GROUP1_IDENTIFIER, JvmControlOperation.START);
        @SuppressWarnings("deprecation")
        GroupJvmDispatchCommand groupDispatchCommand = new GroupJvmDispatchCommand(theGroup, startGroupCommand, new User("hardCodedUser"));

        jvmCommandCompletionChannel.subscribe(new TestMessageHandler());

        gateway.asyncDispatchCommand(groupDispatchCommand); // do it...

        // wait for aggregator to respond...
        Message<?> aggregatorResponse = blockingQueue.poll(5, TimeUnit.SECONDS);

        assertNotNull(aggregatorResponse);
        
        @SuppressWarnings("unchecked")
        List<JvmDispatchCommandResult> aggregatedDispatchCmdList = (List<JvmDispatchCommandResult>) aggregatorResponse
                .getPayload();

        assertEquals(2, aggregatedDispatchCmdList.size());

        for (JvmDispatchCommandResult jvmDispatchCommandResult : aggregatedDispatchCmdList) {
//            assertTrue(jvmDispatchCommandResult.wasSuccessful());
            assertEquals(groupDispatchCommand, jvmDispatchCommandResult.getGroupJvmDispatchCommand());
            // TODO : need to assert I got back the correct list of jvms. Right
            // now the mock returns the same result (JVM1 id) for both calls to
            // JvmControlService. (but I am getting back the correct messages)
        }
    }

    @Test
    public void splitGroupIntoTwoWebServerControls() throws InterruptedException {
        mockWs1 = mock(WebServer.class);
        when(mockWs1.getId()).thenReturn(WS1_IDENTIFIER);
        mockWs2 = mock(WebServer.class);
        when(mockWs2.getId()).thenReturn(WS2_IDENTIFIER);

        wsList.add(mockWs1);
        wsList.add(mockWs2);

        ControlGroupWebServerRequest startGroupCommand = new ControlGroupWebServerRequest(GROUP1_IDENTIFIER, WebServerControlOperation.START);
        @SuppressWarnings("deprecation")
        GroupWebServerDispatchCommand groupDispatchCommand = new GroupWebServerDispatchCommand(theGroup, startGroupCommand, new User("hardCodedUser"));

        wsCommandCompletionChannel.subscribe(new TestMessageHandler());

        gateway.asyncDispatchCommand(groupDispatchCommand); // do it...

        // wait for aggregator to respond...
        Message<?> aggregatorResponse = blockingQueue.poll(5, TimeUnit.SECONDS);

        assertNotNull(aggregatorResponse);
        
        @SuppressWarnings("unchecked")
        List<WebServerDispatchCommandResult> aggregatedDispatchCmdList = (List<WebServerDispatchCommandResult>) aggregatorResponse.getPayload();

        assertEquals(2, aggregatedDispatchCmdList.size());

        for (WebServerDispatchCommandResult webServerDispatchCommandResult : aggregatedDispatchCmdList) {
//            assertTrue(webServerDispatchCommandResult.wasSuccessful());
            assertEquals(groupDispatchCommand, webServerDispatchCommandResult.getGroupWebServerDispatchCommand());
            // TODO : need to assert I got back the correct list of jvms. Right
            // now the mock returns the same result (JVM1 id) for both calls to
            // JvmControlService. (but I am getting back the correct messages)
        }
    }

    class TestMessageHandler implements MessageHandler {
        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            blockingQueue.add(message);
        }
    }

    @Configuration
    @ImportResource("classpath*:META-INF/spring/integration.xml")
    static class CommonConfiguration {

        @Bean 
        public static PropertySourcesPlaceholderConfigurer configurer() { 
             PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
             ppc.setLocation(new ClassPathResource("META-INF/spring/toc-defaults.properties"));
             ppc.setLocalOverride(true);
             return ppc;
        }

        @Bean(name = "jvmControlService")
        public JvmControlService jvmControlService() {
            JvmControlService mockJvmControlService = mock(JvmControlService.class);
            return mockJvmControlService;
        }

        @Bean(name = "groupJvmControlService")
        public GroupJvmControlService groupJvmControlService() {
            GroupJvmControlService mockGroupJvmControlService = mock(GroupJvmControlService.class);
            return mockGroupJvmControlService;
        }

        @SuppressWarnings("unchecked")
        @Bean(name = "webServerPersistenceService")
        public WebServerPersistenceService getWebServerPersistenceService() {
            WebServerPersistenceService mockWebServerPersistenceService = mock(WebServerPersistenceService.class);
            when(mockWebServerPersistenceService.findWebServersBelongingTo(any(Identifier.class))).thenReturn(wsList);
            return mockWebServerPersistenceService;
        }

        @SuppressWarnings("unchecked")
        @Bean(name = "jvmPersistenceService")
        public JvmPersistenceService getJvmPersistenceService() {
            JvmPersistenceService mockJvmPersistenceService = mock(JvmPersistenceService.class);
            when(mockJvmPersistenceService.findJvmsBelongingTo(any(Identifier.class))).thenReturn(jvmList);
            return mockJvmPersistenceService;
        }
        
        @Bean(name="webServerControlService")
        public WebServerControlService getWebServerControlService() {
            CommandOutput execData = new CommandOutput(new ExecReturnCode(0), "Successful.", "");
            WebServerControlService mockWebServerControlService = mock(WebServerControlService.class);
            return mockWebServerControlService;
        }

        @Bean(name="groupWebServerControlService")
        public GroupWebServerControlService getGroupWebServerControlService() {
            GroupWebServerControlService mockGroupWebServerControlService = mock(GroupWebServerControlService.class);
            return mockGroupWebServerControlService;
        }
    }
}
