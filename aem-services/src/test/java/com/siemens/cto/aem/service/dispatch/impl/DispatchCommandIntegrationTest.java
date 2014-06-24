package com.siemens.cto.aem.service.dispatch.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.SplittableDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.SplitterTransformer;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { DispatchCommandIntegrationTest.CommonConfiguration.class })
public class DispatchCommandIntegrationTest {

    private static final Identifier<Jvm> JVM1_IDENTIFIER = new Identifier<>((long) 1);
    private static final Identifier<Jvm> JVM2_IDENTIFIER = new Identifier<>((long) 2);
    private BlockingQueue<Message<?>> blockingQueue;
    
    @Autowired
    private CommandDispatchGateway gateway;

    @Autowired
    @Qualifier("command-completion")
    private DirectChannel commandCompletionChannel;

    private Jvm mockJvm1;
    private Jvm mockJvm2;
    private TestMessageHandler handler;
    
    @Before
    public void setup() {
        mockJvm1 = mock(Jvm.class);
        mockJvm2 = mock(Jvm.class);
        blockingQueue = new ArrayBlockingQueue<>(1);
        handler = new TestMessageHandler();
        commandCompletionChannel.subscribe(handler);
    }

    @Test
    public void splitGroupIntoTwoJvmControls() throws InterruptedException {
        
        gateway.asyncDispatchCommand(createSplittable());
        
        Message<?> aggregatorResponse = blockingQueue.poll(3, TimeUnit.SECONDS);  // added to queue from another thread.
        
        @SuppressWarnings("unchecked")
        List<CompleteControlJvmCommand> aggregatedList = (List<CompleteControlJvmCommand>) aggregatorResponse.getPayload();
        assertEquals(2, aggregatedList.size());

        for (CompleteControlJvmCommand completeControlJvmCommand : aggregatedList) {
            ExecData execData = completeControlJvmCommand.getExecData();
            ExecReturnCode returnCode = execData.getReturnCode();
            assertTrue(returnCode.wasCompleted());
            assertTrue(returnCode.wasSuccessful());
            
            assertEquals(new Long(99), completeControlJvmCommand.getControlHistoryId().getId());
        }
        System.out.println("Test Complete!");
    }
    
    class TestMessageHandler implements MessageHandler {
        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            blockingQueue.add(message);
        }
    }

    protected SplittableDispatchCommand createSplittable() {
        return new SplittableDispatchCommand() {
            @Override
            public long getIdentity() {
                return new Long(99);
            }

            private static final long serialVersionUID = 1L;

            @Override
            public List<DispatchCommand> getSubCommands(SplitterTransformer splitter) {
                List<DispatchCommand> subCmds = new ArrayList<DispatchCommand>();
                subCmds.add(new JvmDispatchCommand(mockJvm1, new ControlJvmCommand(JVM1_IDENTIFIER, JvmControlOperation.START)));
                subCmds.add(new JvmDispatchCommand(mockJvm2, new ControlJvmCommand(JVM2_IDENTIFIER, JvmControlOperation.START)));
                return subCmds;
            }
        };
    }

    @Configuration
    @ImportResource("classpath*:META-INF/spring/integration.xml")
    static class CommonConfiguration {
    }
  }
