package com.siemens.cto.aem.service.dispatch.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.store.MessageGroup;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.SplittableDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.SplitterTransformer;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.service.configuration.service.AemIntegrationConfig;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.dispatch.ExecutorGatewayBean;
import com.siemens.cto.aem.service.dispatch.impl.CommandExecutionMessageStore;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    DispatchCommandIntegrationTest.CommonConfiguration.class,
    AemIntegrationConfig.class 
    })
// @IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION) 
@RunWith(SpringJUnit4ClassRunner.class)
public class DispatchCommandIntegrationTest implements ApplicationContextAware {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DispatchCommandIntegrationTest.class);

    @Configuration
    static class CommonConfiguration {

        @Bean
        public SshConfiguration getSshConfiguration() {

            final SshConfiguration configuration = new SshConfiguration("na",
                                                                        22,
                                                                        "private key file",
                                                                        "known hosts");

            return configuration;
        }

    }   

    @Test
    public void testContextLoading() {
        // intentionally blank
    }

    @Test
    public void testAccessGateway() {
        
        CommandDispatchGateway gateway = context.getBean("commandDispatch", CommandDispatchGateway.class);
        // gateway.dispatchCommand(new DispatchCommand("net", "test"), "deploy");
        gateway.asyncDispatchCommand(new JvmDispatchCommand(){

            @Override public String toString() { return "test-jvm-dispatch-1"; }
            private static final long serialVersionUID = 1L;
        }/*, "deploy" */);
    }
    
    @Test
    public void testAccessCompletions() throws InterruptedException {
        
        assertNotNull(commandExecutionMessageStore);
        commandExecutionMessageStore.expireMessageGroups(-1);
        
        LOGGER.info("testAccessCompletions starting");
        CommandDispatchGateway gateway = context.getBean("commandDispatch", CommandDispatchGateway.class);
        gateway.asyncDispatchCommand(new SplittableDispatchCommand() {

            @Override public String toString() { return "test-jvm-dispatch-async"; }
            private static final long serialVersionUID = 1L;
            
            @Override public List<DispatchCommand> getSubCommands(SplitterTransformer splitter) {
                ArrayList<DispatchCommand> jvms = new ArrayList<>();
                jvms.add(new JvmDispatchCommand());
                jvms.add(new JvmDispatchCommand());
                jvms.add(new JvmDispatchCommand());
                jvms.add(new JvmDispatchCommand());
                return jvms;
            }
        }/*, "deploy" */);

        // First wait until we are sure the message is being processed

        // if splitter is blocking, we should be blocked and so the work should already be done
        // and we could use: long timeout = 0;
        // But it is NOT (hence the method name asyncDispatch), so we will have to wait.        
        long timeout = System.currentTimeMillis() + 2500;

        while( System.currentTimeMillis() < timeout ) {
            
            if(commandExecutionMessageStore.getMessageGroupCount() > 0) break;

            Thread.sleep(20);
        }

        LOGGER.info("Group found in message store, or timed out");
        
        timeout = System.currentTimeMillis() + 2500;
        while( System.currentTimeMillis() < timeout ) {
                    
            for(MessageGroup group : commandExecutionMessageStore) {
    
                if(group.isComplete()) {
                    LOGGER.info("Group complete: " + group.toString());
                
                    return ;
                }
            }

            Thread.sleep(20);            
        }
        
        fail("Should not complete without finding a group");
                
    }


    @Autowired CommandExecutionMessageStore commandExecutionMessageStore;    
    
    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        
    }
    
    @Test
    @Ignore
    public void testSynchronousGateway() throws InterruptedException {
        fail("Not implemented yet");
    }
}