package com.siemens.cto.aem.service.state.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.toc.files.configuration.TocFileManagerConfigReference;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    GroupStateServiceStateMachineIntegrationTest.CommonConfiguration.class,
    TestJpaConfiguration.class, TocFileManagerConfigReference.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
public class GroupStateServiceStateMachineIntegrationTest {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateServiceStateMachineIntegrationTest.class);

    // Parallelization
    ScheduledExecutorService concurrencyActions = java.util.concurrent.Executors.newScheduledThreadPool(10);

    // Entity state follows
    Object groupLock = new Object(), groupWith3Lock = new Object();
    Group group, groupWith3;
    LiteGroup lgroup, lgroupWith3;
    Set<LiteGroup> lgroups = new HashSet<>(), lgroupsWith3 = new HashSet<>();
    Jvm jvm, jvm2, jvm3, jvm4;
    JvmState[] currentJvmStates = new JvmState[] {
        JvmState.INITIALIZED,
        JvmState.INITIALIZED,
        JvmState.INITIALIZED,
        JvmState.INITIALIZED,
        JvmState.INITIALIZED
    };
    Set<Jvm> jvms= new HashSet<>(), jvmsThree= new HashSet<>();

    @Autowired
    @Qualifier("jvmStateUpdates")
    SubscribableChannel stateUpdates;

    @Autowired
    StateNotificationGateway stateNotificationGateway;

    @Autowired
    JvmPersistenceService jvmPersistenceService;

    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    TaskExecutor statePubSubBusExecutor;

    @Autowired
    GroupStateService.API   groupStateService;

    @Autowired
    GroupStateMachine   groupStateManagerTableImpl;

    @Autowired
    @Qualifier("jvmStatePersistenceService")
    StatePersistenceService<Jvm, JvmState> jvmStatePersistenceService;

    // test state
    int updateReceived = 0;

    @SuppressWarnings("unchecked")
    @Before
    public void setupEntities() throws InterruptedException {

        group = new Group(id(1L, Group.class), "" );
        groupWith3 = new Group(id(2L, Group.class), "");
        lgroup = new LiteGroup(id(1L, Group.class), "");
        lgroupWith3 = new LiteGroup(id(2L, Group.class), "");
        lgroups.add(lgroup);
        lgroupsWith3.add(lgroupWith3);
        jvm = new Jvm(id(1L, Jvm.class), "", "", lgroups, 0,0,0,0,0, new Path("/abc"));
        jvms.add(jvm);
        jvm2 = new Jvm(id(2L, Jvm.class), "", "", lgroupsWith3, 0,0,0,0,0, new Path("/abc"));
        jvmsThree.add(jvm2);
        jvm3 = new Jvm(id(3L, Jvm.class), "", "", lgroupsWith3, 0,0,0,0,0, new Path("/abc"));
        jvmsThree.add(jvm3);
        jvm4 = new Jvm(id(4L, Jvm.class), "", "", lgroupsWith3, 0,0,0,0,0, new Path("/abc"));
        jvmsThree.add(jvm4);
        group = new Group(group.getId(),  group.getName(), jvms);
        groupWith3 = new Group(groupWith3.getId(),  groupWith3.getName(), jvmsThree);

        when(jvmPersistenceService.getJvm(eq(jvm.getId()))).thenReturn(jvm);
        when(jvmPersistenceService.getJvm(eq(jvm2.getId()))).thenReturn(jvm2);
        when(jvmPersistenceService.getJvm(eq(jvm3.getId()))).thenReturn(jvm3);
        when(jvmPersistenceService.getJvm(eq(jvm4.getId()))).thenReturn(jvm4);
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        when(groupPersistenceService.getGroup(eq(groupWith3.getId()))).thenReturn(groupWith3);

        when(groupPersistenceService.updateGroupStatus(Mockito.any(Event.class))).thenAnswer(new Answer<Group>() {

            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {

                if(invocation.getArguments().length == 0 || invocation.getArguments()[0] == null) {
                    LOGGER.error("Error could not find data arguments to updateState!");
                    return null;
                } else {
                    LOGGER.info("Persisting data...");
                }
                Event<SetGroupStateCommand> event = (Event<SetGroupStateCommand>) invocation.getArguments()[0];
                Identifier<Group> groupId = event.getCommand().getNewState().getId();
                LOGGER.info("Persisting data for groupId " + groupId.getId());
                LOGGER.info("Persisting data for groupId " + groupId.getId() + " state " + event.getCommand().getNewState().getState());
                if(groupId.getId() == 1L) {
                    synchronized(groupLock) {
                        group = new Group(group.getId(), group.getName(), group.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                        groupLock.notifyAll();
                        return group;
                    }
                } else if(groupId.getId() == 2L) {
                    synchronized(groupWith3Lock) {
                        groupWith3 = new Group(groupWith3.getId(), groupWith3.getName(), groupWith3.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                        groupWith3Lock.notifyAll();
                        return groupWith3;
                    }
                }
                fail("Unexpected groupId " + group.getId());
                return null;
            }

        });

        when(jvmStatePersistenceService.getState(Mockito.any(Identifier.class))).thenAnswer(new Answer<CurrentState<Jvm, JvmState>>() {

            @Override
            public CurrentState<Jvm, JvmState> answer(InvocationOnMock invocation) throws Throwable {
                if(invocation.getArguments().length == 0 || invocation.getArguments()[0] == null) {
                    LOGGER.error("Error could not find id argument to getJvmState!");
                    return null;
                }                Identifier<Jvm> idJvm = (Identifier<Jvm>) invocation.getArguments()[0];
                int id = idJvm.getId().intValue();
                return new CurrentState<>(idJvm, currentJvmStates[id], DateTime.now(), StateType.JVM);
            }

        });


        // test configuration.
        scheduleJvmThread(1L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        waitForState(groupLock, "GroupStateServiceStateMachineIntegrationTest.* INIT", System.currentTimeMillis(), 10000, 1L, GroupState.STARTED);
    }

    @After
    public void settle() throws InterruptedException {
        Thread.sleep(50);
    }

    @Test
    /**
     * Note: this test claims to be 'in Series' but it
     * is dangerously close to ending up being parallel,
     * so some intermittent failures might occur.
     */
    public void testGroupStateUpdatingSeries() throws InterruptedException {

        long await = System.currentTimeMillis();

        scheduleJvmThread(2L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STARTED, 5, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STARTED, 10, TimeUnit.MILLISECONDS);

        waitForCompletion(groupWith3Lock, "testGroupStateUpdatingSeries", await-10, 1000);

        CurrentGroupState state = groupWith3.getCurrentState();
        assertEquals(GroupState.STARTED, state.getState());

        scheduleJvmThread(2L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STOPPED, 5, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STOPPED, 10, TimeUnit.MILLISECONDS);

        waitForState(groupWith3Lock, "testGroupStateUpdatingSeries", await-10, 1000, 2L, GroupState.STOPPED);

        state = groupWith3.getCurrentState();
        assertEquals(GroupState.STOPPED, state.getState());
    }

    @Test
    /**
     * Note: this test claims to be 'in Series' but it
     * is dangerously close to ending up being parallel,
     * so some intermittent failures might occur.
     */
    public void testGroupStateParallelStartStop() throws InterruptedException {

        long await = System.currentTimeMillis();

        groupStateService.signalStartRequested(groupWith3.getId(), User.getSystemUser());

        scheduleJvmThread(2L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);

        waitForState(groupWith3Lock, "testGroupStateParallelStartStop", await, 1000, 2L, GroupState.STARTED);

        CurrentGroupState state = groupWith3.getCurrentState();
        assertEquals(GroupState.STARTED, state.getState());
        assertTrue(groupStateService.canStop(groupWith3.getId(), User.getSystemUser()));
        groupStateService.signalStopRequested(groupWith3.getId(), User.getSystemUser());

        scheduleJvmThread(2L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);


        waitForState(groupWith3Lock, "testGroupStateParallelStartStop", await, 1000, 2L, GroupState.STOPPED);

        state = groupWith3.getCurrentState();
        assertEquals(GroupState.STOPPED, state.getState());
        assertTrue(groupStateService.canStart(groupWith3.getId(), User.getSystemUser()));
    }

    @Ignore // very very slow to run on the server, likely fails
    @Test
    public void testGroupStateUpdatingInParallel() throws InterruptedException {

        Thread.sleep(50);

        long await = System.currentTimeMillis();

        scheduleJvmThread(2L, JvmState.STARTED, 9, TimeUnit.MILLISECONDS);
        scheduleJvmThread(2L, JvmState.STOPPED, 10, TimeUnit.MILLISECONDS);

        scheduleJvmThread(3L, JvmState.STARTED, 9, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STOPPED, 10, TimeUnit.MILLISECONDS);

        scheduleJvmThread(4L, JvmState.STARTED, 9, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STOPPED, 10, TimeUnit.MILLISECONDS);

        waitForState(groupWith3Lock, "testGroupStateUpdatingInParallel", await-10, 500, 2L, GroupState.STOPPED);

        CurrentGroupState state = groupWith3.getCurrentState();
        assertEquals(GroupState.STOPPED, state.getState());

        assertTrue(groupStateService.canStart(groupWith3.getId(), User.getSystemUser()));
    }

    // test helpers
    private void waitForState(Object lock, String test, long start, int maxDuration, long groupId, GroupState state) throws InterruptedException {
        long end = start + maxDuration;
        while(System.currentTimeMillis() < end && getStateForGroup(groupId) != state)  {
            synchronized(lock) {
                lock.wait(maxDuration*2);
            }
        }
        long latency = (System.currentTimeMillis() - start);
        if(getStateForGroup(groupId) != state) {
            fail("TIMING: "+test+" latency>"+maxDuration +"ms (" + latency + "ms) waiting for " + state);
        }
        LOGGER.info("TIMING: "+test+" latency = " + latency + "ms waiting for " + state);
    }

    private GroupState getStateForGroup(long groupId) {
        return groupId == 1L ?group.getCurrentState().getState() :
            groupId == 2L ? groupWith3.getCurrentState().getState() : null;
    }

    private void waitForCompletion(Object lock, String test, long start, int maxDuration) throws InterruptedException {
        synchronized(lock) {
            lock.wait(maxDuration*2);
        }
        long latency = (System.currentTimeMillis() - start);
        if(latency > maxDuration) {
            fail("TIMING: "+test+" latency>"+maxDuration +"ms (" + latency + "ms)");
        }
        LOGGER.info("TIMING: "+test+" latency = " + latency + "ms");
    }

    private void scheduleJvmThread(final long id, final JvmState state, final long delay, final TimeUnit units) {
        concurrencyActions.schedule(
                Executors.callable(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("stateNotificationGateway.jvmStateChanged("+id+") = " + state);
                currentJvmStates[(int)id] = state;
                stateNotificationGateway.jvmStateChanged(new CurrentState<>(id(id, Jvm.class), state, DateTime.now(), StateType.JVM));
            }
        })
        , delay, units);
    }


    @Configuration
    @ImportResource("classpath*:META-INF/spring/integration-state.xml")
    static class CommonConfiguration {

        @Autowired
        StateNotificationGateway stateNotification;

        @Bean(name="groupStateMachine")
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public GroupStateMachine getGroupStateManagerTableImpl() {
            return new GroupStateManagerTableImpl();
        }
        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return Mockito.mock(JvmPersistenceService.class);
        }

        @SuppressWarnings("unchecked")
        @Bean(name = "jvmStatePersistenceService")
        public StatePersistenceService<Jvm, JvmState> getJvmStatePersistenceService() {
            return (StatePersistenceService<Jvm, JvmState>)Mockito.mock(StatePersistenceService.class);
        }

        @Bean(name = "groupPersistenceService")
        public GroupPersistenceService getGroupPersistenceService() {
            return Mockito.mock(GroupPersistenceService.class);
        }
        @Bean
        public WebServerDao getWebServerDao() {
            return Mockito.mock(WebServerDao.class);
        }

        @SuppressWarnings("unchecked")
        @Bean(name = "webServerStateService")
        public StateService<WebServer, WebServerReachableState>    getWebServerStateService() {
            return Mockito.mock(StateService.class);
        }

        @Bean(name = "stateNotificationService")
        public StateNotificationService getStateNotificationService() {
            return Mockito.mock(StateNotificationService.class);
        }

        @Bean(name = "groupStateService")
        public GroupStateService.API getGroupStateService() {
            return new GroupStateServiceImpl(
                    getGroupPersistenceService(),
                    getStateNotificationService(),
                    StateType.GROUP,
                    stateNotification
                    );
        }
    }
}
