package com.siemens.cto.aem.service.state.impl;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
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


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    GroupStateServiceStateMachineIntegrationTest.CommonConfiguration.class,
    TestJpaConfiguration.class, TocFileManagerConfigReference.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EnableTransactionManagement
public class GroupStateServiceStateMachineIntegrationTest {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateServiceStateMachineIntegrationTest.class);

    // Parallelization
    ScheduledExecutorService concurrencyActions = java.util.concurrent.Executors.newScheduledThreadPool(10);

    // Entity state follows
    Semaphore groupLock = new Semaphore(0), group2Lock = new Semaphore(0), group2bLock = new Semaphore(0), groupWith3Lock = new Semaphore(0);
    Group group, group2, groupWith3, group2b;
    LiteGroup lgroup, lgroup2, lgroup2b, lgroupWith3;
    Set<LiteGroup> lgroups = new HashSet<>(), lgroups2 = new HashSet<>(), lgroupsWith3 = new HashSet<>();
    Jvm jvm, jvm2, jvm3, jvm4, jvm5;
    JvmState[] currentJvmStates = new JvmState[] { 
        JvmState.INITIALIZED, 
        JvmState.INITIALIZED, 
        JvmState.INITIALIZED, 
        JvmState.INITIALIZED, 
        JvmState.INITIALIZED,        
        JvmState.INITIALIZED        
    };
    Set<Jvm> jvms= new HashSet<>(), jvmInTwoGroups = new HashSet<>(), jvmsThree= new HashSet<>();

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
        group2 = new Group(id(3L, Group.class), "" );
        groupWith3 = new Group(id(2L, Group.class), "");
        group2b = new Group(id(4L, Group.class), "");
        lgroup = new LiteGroup(id(1L, Group.class), "");
        lgroup2 = new LiteGroup(id(3L, Group.class), "");
        lgroup2b = new LiteGroup(id(4L, Group.class), "");
        lgroupWith3 = new LiteGroup(id(2L, Group.class), "");
        lgroups.add(lgroup);
        lgroups2.add(lgroup2);
        lgroups2.add(lgroup2b);
        lgroupsWith3.add(lgroupWith3);
        jvm = new Jvm(id(1L, Jvm.class), "", "", lgroups, 0,0,0,0,0, new Path("/hct"));
        jvms.add(jvm);
        jvm2 = new Jvm(id(2L, Jvm.class), "", "", lgroupsWith3, 0,0,0,0,0, new Path("/hct"));
        jvmsThree.add(jvm2);
        jvm3 = new Jvm(id(3L, Jvm.class), "", "", lgroupsWith3, 0,0,0,0,0, new Path("/hct"));
        jvmsThree.add(jvm3);
        jvm4 = new Jvm(id(4L, Jvm.class), "", "", lgroupsWith3, 0,0,0,0,0, new Path("/hct"));
        jvmsThree.add(jvm4);
        jvm5 = new Jvm(id(5L, Jvm.class), "", "", lgroups2, 0,0,0,0,0, new Path("/hct"));
        jvmInTwoGroups.add(jvm5);
        group = new Group(group.getId(),  group.getName(), jvms);
        group2 = new Group(group2.getId(), group2.getName(), jvmInTwoGroups);
        group2b = new Group(group2b.getId(), group2b.getName(), jvmInTwoGroups);
        groupWith3 = new Group(groupWith3.getId(),  groupWith3.getName(), jvmsThree);

        when(jvmPersistenceService.getJvm(eq(jvm.getId()))).thenReturn(jvm);
        when(jvmPersistenceService.getJvm(eq(jvm2.getId()))).thenReturn(jvm2);
        when(jvmPersistenceService.getJvm(eq(jvm3.getId()))).thenReturn(jvm3);
        when(jvmPersistenceService.getJvm(eq(jvm4.getId()))).thenReturn(jvm4);
        when(jvmPersistenceService.getJvm(eq(jvm5.getId()))).thenReturn(jvm5);
        when(groupPersistenceService.getGroup(eq(group.getId()))).thenReturn(group);
        when(groupPersistenceService.getGroup(eq(group2.getId()))).thenReturn(group2);
        when(groupPersistenceService.getGroup(eq(group2b.getId()))).thenReturn(group2b);
        when(groupPersistenceService.getGroup(eq(groupWith3.getId()))).thenReturn(groupWith3);

        when(groupPersistenceService.updateGroupStatus(Mockito.any(Event.class))).thenAnswer(new Answer<Group>() {

            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {

                if(invocation.getArguments().length == 0 || invocation.getArguments()[0] == null) {                    
                    LOGGER.error("Error could not find data arguments to updateState!");
                    return null;
                }
                Event<SetGroupStateCommand> event = (Event<SetGroupStateCommand>) invocation.getArguments()[0];
                Identifier<Group> groupId = event.getCommand().getNewState().getId(); 
                LOGGER.info("Persisting data for groupId " + groupId.getId() + " state " + event.getCommand().getNewState().getState());
                if(groupId.getId() == 1L) {
                    group = new Group(group.getId(), group.getName(), group.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                    groupLock.release(1);
                    return group;
                } else if(groupId.getId() == 2L) {
                    groupWith3 = new Group(groupWith3.getId(), groupWith3.getName(), groupWith3.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                    groupWith3Lock.release(1);
                    return groupWith3;
                    
                } else if(groupId.getId() == 3L) {
                    group2 = new Group(group2.getId(), group2.getName(), group2.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                    group2Lock.release(1);
                    return group2;
                } else if(groupId.getId() == 4L) {
                    group2b = new Group(group2b.getId(), group2b.getName(), group2b.getJvms(), event.getCommand().getNewState().getState(), DateTime.now());
                    group2bLock.release(1);
                    return group2b;
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
                return new CurrentState<Jvm, JvmState>(idJvm, currentJvmStates[id], DateTime.now(), StateType.JVM);
            }
            
        });


        // test configuration.
        scheduleJvmThread(1L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        waitForLocks(1L, "GroupStateServiceStateMachineIntegrationTest.* INIT", 1, System.currentTimeMillis(), 10000, 10000);
        
        settle();
    }

    
    @Before
    @After
    public void settle() throws InterruptedException { 
        groupLock.drainPermits();
        group2Lock.drainPermits();
        group2bLock.drainPermits();
        groupWith3Lock.drainPermits();
    }

    @Ignore // due to anticipated intermittent failures.
    @Test
    /**
     * Note: this test claims to be 'in Series' but it 
     * is dangerously close to ending up being parallel,
     * so some intermittent failures might occur.
     */
    public void testGroupStateUpdatingSeries() throws InterruptedException {

        long await = System.currentTimeMillis();

        await = System.currentTimeMillis();

        groupWith3Lock.drainPermits();

        scheduleJvmThread(2L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STARTED, 5, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STARTED, 10, TimeUnit.MILLISECONDS);

        waitForLocks(2, "testGroupStateUpdatingSeries", 3, await-10, 5000, 5000);

        CurrentGroupState state = groupWith3.getCurrentState();
        assertEquals(GroupState.STARTED, state.getState());
        
        settle();

        scheduleJvmThread(2L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STOPPED, 5, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STOPPED, 10, TimeUnit.MILLISECONDS);

        waitForLocks(2, "testGroupStateUpdatingSeries", 3, await-10, 5000, 5000);
       
        state = groupWith3.getCurrentState();
        // I just want consistency at the time we query. Which still might not be enough time for it to take effect
        // but it is more likely to succeed especially if spring integration is configured with direct channels.
        assertEquals(((currentJvmStates[2] == currentJvmStates[3]) && ( currentJvmStates[3] == currentJvmStates[4])) ? GroupState.valueOf(currentJvmStates[3].toStateString()) : GroupState.PARTIAL, state.getState());
        assertEquals(GroupState.STOPPED, state.getState());
    }
    

    @Test
    public void testJvmInTwoGroups() throws InterruptedException {

        long await = System.currentTimeMillis();

        await = System.currentTimeMillis();

        scheduleJvmThread(5L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);

        waitForState(group2Lock, "testJvmInTwoGroups", await-10, 10000, 3L, GroupState.STARTED);
        waitForState(group2bLock, "testJvmInTwoGroups", await-10, 10000, 4L, GroupState.STARTED);

        CurrentGroupState state = group2.getCurrentState();
        assertEquals(GroupState.STARTED, state.getState());
        state = group2b.getCurrentState();
        assertEquals(GroupState.STARTED, state.getState());

        group2Lock.drainPermits();
        group2bLock.drainPermits();
        
        scheduleJvmThread(5L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);

        waitForState(group2Lock, "testJvmInTwoGroups", await-10, 10000, 3L, GroupState.STOPPED);
        waitForState(group2bLock, "testJvmInTwoGroups", await-10, 10000, 4L, GroupState.STOPPED);
        
        state = group2.getCurrentState();
        assertEquals(GroupState.STOPPED, state.getState());
        state = group2b.getCurrentState();
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
        
        groupWith3Lock.drainPermits();

        scheduleJvmThread(2L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STARTED, 0, TimeUnit.MILLISECONDS);

        waitForState(groupWith3Lock, "testGroupStateParallelStartStop", await, 10000, 2L, GroupState.STARTED);
        
        CurrentGroupState state = groupWith3.getCurrentState();
        assertEquals(GroupState.STARTED, state.getState());
        assertTrue(groupStateService.canStop(groupWith3.getId(), User.getSystemUser()));
        groupStateService.signalStopRequested(groupWith3.getId(), User.getSystemUser());

        groupWith3Lock.drainPermits();

        scheduleJvmThread(2L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(3L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);
        scheduleJvmThread(4L, JvmState.STOPPED, 0, TimeUnit.MILLISECONDS);


        waitForState(groupWith3Lock, "testGroupStateParallelStartStop", await, 10000, 2L, GroupState.STOPPED);
        
        state = groupWith3.getCurrentState();
        assertEquals(((currentJvmStates[2] == currentJvmStates[3]) && ( currentJvmStates[3] == currentJvmStates[4])) ? GroupState.valueOf(currentJvmStates[3].toStateString()) : GroupState.PARTIAL, state.getState());
        assertTrue(groupStateService.canStart(groupWith3.getId(), User.getSystemUser()));
    }

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
        
        waitForLocks(2, "testGroupStateUpdatingInParallel", 6, await-10, 5000, 5000);

        CurrentGroupState state = groupWith3.getCurrentState();        
        assertEquals(((currentJvmStates[2] == currentJvmStates[3]) && ( currentJvmStates[3] == currentJvmStates[4])) ? GroupState.valueOf(currentJvmStates[3].toStateString()) : GroupState.PARTIAL, state.getState());
        
        assertTrue(groupStateService.canStart(groupWith3.getId(), User.getSystemUser()));
    }

    // test helpers
    private void waitForState(Object lock, String test, long start, int maxDuration, long groupId, GroupState state) throws InterruptedException {
        long end = System.currentTimeMillis() + maxDuration;
        while(System.currentTimeMillis() < end && getStateForGroup(groupId) != state)  {
            waitForLocks(groupId, test, 1, start, maxDuration/5, maxDuration);
        }
        long latency = (System.currentTimeMillis() - start);
        if(getStateForGroup(groupId) != state) {            
            fail("TIMING: "+test+" latency>"+maxDuration +"ms (" + latency + "ms) waiting for " + state);
        }
        LOGGER.info("TIMING: "+test+" latency = " + latency + "ms waiting for " + state);
    }
    
    private void waitForLocks(long groupId, String test, int count, long start, int maxDuration, int maxLatencyFromAwait) throws InterruptedException {
        if(!( groupId == 1L ? groupLock : 
            groupId == 2L ? groupWith3Lock: 
            groupId == 3L ? group2Lock: 
            groupId == 4L ? group2bLock:
                new Semaphore(count) ).tryAcquire(count, maxDuration, TimeUnit.MILLISECONDS)) {
            long latency = (System.currentTimeMillis() - start);
            fail("TIMING: "+test+" latency>"+maxDuration +"ms (" + latency + "ms) waiting for state update.");
        }
        long latency = (System.currentTimeMillis() - start);
        if(latency > maxLatencyFromAwait) {
            fail("TIMING: "+test+" latency>"+maxDuration +"ms (" + latency + "ms) waiting for state update.");            
        }
        LOGGER.info("TIMING: "+test+" latency = " + latency + "ms waiting for waiting for state update.");
    }
    
    private GroupState getStateForGroup(long groupId) { 
        return  groupId == 1L ?group.getCurrentState().getState() : 
                groupId == 2L ? groupWith3.getCurrentState().getState() : 
                groupId == 3L ? group2.getCurrentState().getState() : 
                groupId == 4L ? group2b.getCurrentState().getState() : 
                null;
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
