package com.siemens.cto.aem.service.dispatch.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.jvm.JvmControlService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { DispatchCommandIntegrationTest.CommonConfiguration.class })
public class DispatchCommandIntegrationTest {

    private static final Identifier<Jvm> JVM1_IDENTIFIER = new Identifier<>((long) 1);
    private static final Identifier<Jvm> JVM2_IDENTIFIER = new Identifier<>((long) 2);
    private static final Identifier<Group> GROUP1_IDENTIFIER = new Identifier<>((long) 11);
    private BlockingQueue<Message<?>> blockingQueue;

    @Autowired
    private CommandDispatchGateway gateway;

    @Autowired
    @Qualifier("jvmAggregated")
    private DirectChannel commandCompletionChannel;

    private Jvm mockJvm1;
    private Jvm mockJvm2;
    private Set<Jvm> jvmSet;
    private Group theGroup;
    private ControlGroupCommand startGroupCommand;
    private GroupJvmDispatchCommand groupDispatchCommand;
    private Identifier<GroupControlHistory> theHistoryId;

    @Before
    @SuppressWarnings("deprecation")
    public void setup() {

        mockJvm1 = mock(Jvm.class);
        when(mockJvm1.getId()).thenReturn(JVM1_IDENTIFIER);
        mockJvm2 = mock(Jvm.class);
        when(mockJvm2.getId()).thenReturn(JVM2_IDENTIFIER);

        jvmSet = new HashSet<Jvm>();
        jvmSet.add(mockJvm1);
        jvmSet.add(mockJvm2);

        theGroup = new Group(GROUP1_IDENTIFIER, "group1", jvmSet);
        startGroupCommand = new ControlGroupCommand(GROUP1_IDENTIFIER, JvmControlOperation.START);
        theHistoryId = new Identifier<GroupControlHistory>(new Long(101));
        groupDispatchCommand = new GroupJvmDispatchCommand(theGroup, startGroupCommand, User.getHardCodedUser(),
                theHistoryId);

        blockingQueue = new ArrayBlockingQueue<>(1);
        commandCompletionChannel.subscribe(new TestMessageHandler());
    }

    @Test
    public void splitGroupIntoTwoJvmControls() throws InterruptedException {

        gateway.asyncDispatchCommand(groupDispatchCommand); // do it...

        // wait for aggregator to respond...
        Message<?> aggregatorResponse = blockingQueue.poll(5, TimeUnit.SECONDS);

        @SuppressWarnings("unchecked")
        List<JvmDispatchCommandResult> aggregatedDispatchCmdList = (List<JvmDispatchCommandResult>) aggregatorResponse
                .getPayload();

        assertEquals(2, aggregatedDispatchCmdList.size());

        for (JvmDispatchCommandResult jvmDispatchCommandResult : aggregatedDispatchCmdList) {
            assertTrue(jvmDispatchCommandResult.wasSuccessful());
            assertEquals(groupDispatchCommand, jvmDispatchCommandResult.getGroupJvmDispatchCommand());
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

        @Bean(name = "jvmControlService")
        public JvmControlService jvmControlService() {
            Identifier<JvmControlHistory> jvm1ControlHistoryId = new Identifier<JvmControlHistory>(new Long(101));
            ExecData execData = new ExecData(new ExecReturnCode(0), "Successful.", "");
            JvmControlHistory mockJvmControlHistory = new JvmControlHistory(jvm1ControlHistoryId, JVM1_IDENTIFIER,
                    null, null, execData);
            JvmControlService mockJvmControlService = mock(JvmControlService.class);
            when(mockJvmControlService.controlJvm(any(ControlJvmCommand.class), any(User.class))).thenReturn(
                    mockJvmControlHistory);
            return mockJvmControlService;
        }

        @Bean(name = "groupControlService")
        public GroupControlService groupControlService() {
            GroupControlService mockGroupControlService = mock(GroupControlService.class);
            return mockGroupControlService;
        }
    }
}
