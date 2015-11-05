package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmStateCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.state.StateCrudService;
import com.siemens.cto.aem.persistence.service.jvm.impl.JvmJpaStatePersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.jvm.impl.JvmStateServiceImpl;
import com.siemens.cto.aem.service.state.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {StateServiceImplTest.Config.class
                      })
public class StateServiceImplTest {

    @Autowired
    private StateService<Jvm, JvmState> stateService;

    private User user;

    @Before
    public void setUp() throws Exception {
        user = new User("IntegrationTestUser");
    }

    @Test
    public void testSetCurrentStateWithoutMessage() throws Exception {
        final CurrentState<Jvm, JvmState> newCurrentState = new CurrentState<>(new Identifier<Jvm>(123456L),
                                                                               JvmState.JVM_STARTED,
                                                                               DateTime.now(),
                                                                               StateType.JVM);
        verifySetCurrentState(newCurrentState);
    }

    @Test
    public void testSetCurrentStateWithMessage() throws Exception {
        final CurrentState<Jvm, JvmState> newCurrentState = new CurrentState<>(new Identifier<Jvm>(123456L),
                                                                               JvmState.JVM_STARTED,
                                                                               DateTime.now(),
                                                                               StateType.JVM,
                                                                               "This is the message to persist along with this state");

        verifySetCurrentState(newCurrentState);
    }

    @Test
    public void testGetCurrentState() throws Exception {
        final Identifier<Jvm> jvmId = new Identifier<>(123456L);
        final List<SetStateCommand<Jvm, JvmState>> commands = createCommandsToPersist(jvmId,
                                                                                      10);
        SetStateCommand<Jvm, JvmState> lastCommand = null;
        for (final SetStateCommand<Jvm, JvmState> command : commands) {
            stateService.setCurrentState(command,
                                         user);
            lastCommand = command;
        }

        final CurrentState<Jvm, JvmState> actualState = stateService.getCurrentState(jvmId);
        assertNotNull(lastCommand);
        assertEquals(lastCommand.getNewState(),
                     actualState);
    }

    @Test
    public void testGetCurrentStatesForSpecificJvms() throws Exception {
        final Set<Identifier<Jvm>> jvmIds = createJvmIds(10);
        final Map<Identifier<Jvm>, CurrentState<Jvm, JvmState>> expectedStates = new HashMap<>();
        for (final Identifier<Jvm> jvmId : jvmIds) {
            final List<SetStateCommand<Jvm, JvmState>> commands = createCommandsToPersist(jvmId,
                                                                                          10);
            for (final SetStateCommand<Jvm, JvmState> command : commands) {
                stateService.setCurrentState(command,
                                             user);
                expectedStates.put(jvmId, command.getNewState());
            }
        }

        final Set<CurrentState<Jvm, JvmState>> actualStates = stateService.getCurrentStates(jvmIds);
        for (final CurrentState<Jvm, JvmState> actualState : actualStates) {
            assertEquals(expectedStates.get(actualState.getId()),
                         actualState);
        }
    }

    private Set<Identifier<Jvm>> createJvmIds(final int aNumberToCreate) {
        final Set<Identifier<Jvm>> ids = new HashSet<>(aNumberToCreate);
        for (int i = 1; i <= aNumberToCreate; i++) {
            ids.add(new Identifier<Jvm>((long)i));
        }
        return ids;
    }

    @Test
    public void testGetCurrentStatesWithPagination() throws Exception {

    }

    private void verifySetCurrentState(final CurrentState<Jvm, JvmState> aStateToPersist) {
        final SetStateCommand<Jvm, JvmState> command = new JvmSetStateCommand(aStateToPersist);
        final CurrentState<Jvm, JvmState> persistedState = stateService.setCurrentState(command,
                                                                                        user);
        assertEquals(aStateToPersist,
                     persistedState);
    }

    private List<SetStateCommand<Jvm, JvmState>> createCommandsToPersist(final Identifier<Jvm> aJvmId,
                                                             final int aNumberToCreate) {
        final List<SetStateCommand<Jvm, JvmState>> commands = new ArrayList<>(aNumberToCreate);
        for (int i = 0; i < aNumberToCreate; i++) {
            final CurrentState<Jvm, JvmState> newCurrentState = new CurrentState<>(aJvmId,
                                                                                   JvmState.values()[aNumberToCreate % JvmState.values().length],
                                                                                   DateTime.now(),
                                                                                   StateType.JVM,
                                                                                   "This is the message to persist along with this state " + i);
            final SetStateCommand<Jvm, JvmState> command = new JvmSetStateCommand(newCurrentState);
            commands.add(command);
        }
        return commands;
    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean 
        public static PropertySourcesPlaceholderConfigurer configurer() { 
             PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
             ppc.setLocation(new ClassPathResource("META-INF/spring/toc-defaults.properties"));
             ppc.setLocalOverride(true);
             return ppc;
        } 
        
        @Bean
        public StateService<Jvm, JvmState> getStateService() {
            return new JvmStateServiceImpl(getPersistenceService(),
                                           getStateNotificationService(),
                                           getGroupStateService(),
                                           getStateNotificationWorker());
        }

        @Bean
        public StatePersistenceService<Jvm, JvmState> getPersistenceService() {
            return new JvmJpaStatePersistenceServiceImpl(getStateCrudService());
        }

        @Bean
        public StateCrudService<Jvm, JvmState> getStateCrudService() {
            return new JvmStateCrudServiceImpl();
        }

        @Bean
        public StateNotificationService getStateNotificationService() {
            final StateNotificationService service = mock(StateNotificationService.class);
            return service;
        }

        @Bean
        public GroupStateService.API getGroupStateService() {
            return mock(GroupStateService.API.class);
        }

        @Bean
        public StateNotificationWorker getStateNotificationWorker() {
            return mock(StateNotificationWorker.class);
        }
    }
}
