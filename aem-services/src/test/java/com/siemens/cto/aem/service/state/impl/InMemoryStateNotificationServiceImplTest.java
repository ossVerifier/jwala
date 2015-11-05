package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.*;
import com.siemens.cto.aem.service.webserver.impl.WebServerStateServiceImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryStateNotificationServiceImplTest {

    @Mock
    private StatePersistenceService<WebServer, WebServerReachableState> persistenceService;

    @Mock
    private StateNotificationGateway notificationGateway;

    @Mock
    private GroupStateService.API groupStateService;

    @Mock
    private StateNotificationWorker stateNotificationWorker;

    @Mock
    private StateService<WebServer, WebServerReachableState> stateService;

    private StateNotificationService notificationService;

    @Before
    public void setUp() throws Exception {
        setupStateServiceMock();
        final StateNotificationConsumerBuilder notificationConsumerBuilder = new InMemoryStateNotificationConsumerBuilderImpl(new TimeDuration(5L, TimeUnit.MINUTES),
                                                                                                                              new TimeDuration(30L, TimeUnit.SECONDS));
        notificationService = new InMemoryStateNotificationServiceImpl(notificationConsumerBuilder);
        stateService = new WebServerStateServiceImpl(persistenceService,
                                                     notificationService,
                                                     groupStateService,
                                                     stateNotificationWorker);
    }

    @Test
    public void testNotifyWebServerStateUpdated() throws Exception {
        final int numberOfConsumers = 5;
        final Set<StateNotificationConsumerId> consumerIds = trackAndRegisterConsumers(numberOfConsumers);

        final int numberOfNotifications = 200;
        final Set<Identifier<WebServer>> webServers = trackAndAddNotifications(numberOfNotifications);

        for (final StateNotificationConsumerId consumerId : consumerIds) {
            final List<CurrentState<?, ?>> currentWebServerStates = notificationService.pollUpdatedStates(consumerId,
                                                                                                    new TimeRemainingCalculator(new TimeDuration(1L, TimeUnit.SECONDS)));
            assertEquals(numberOfNotifications,
                         currentWebServerStates.size());
            for (final CurrentState<?, ?> state : currentWebServerStates) {
                assertTrue(webServers.contains(state.getId()));
                assertEquals(WebServerReachableState.WS_REACHABLE,
                             state.getState());
            }
        }
    }

    @Test
    public void testDeregister() throws Exception {
        final int numberOfConsumers = 5;
        final Set<StateNotificationConsumerId> consumerIds = trackAndRegisterConsumers(numberOfConsumers);

        final int numberOfNotifications = 200;
        trackAndAddNotifications(numberOfNotifications);

        for (final StateNotificationConsumerId consumerId : consumerIds) {
            notificationService.deregister(consumerId);
            final List<CurrentState<?, ?>> currentStates = notificationService.pollUpdatedStates(consumerId,
                                                                                           new TimeRemainingCalculator(new TimeDuration(500L, TimeUnit.MILLISECONDS)));
            assertTrue(currentStates.isEmpty());
            assertFalse(notificationService.isValid(consumerId));
        }
    }

    @Ignore
    @Test
    // TODO this test fails intermittently - seemingly more frequently when running a Sonar build, so marking as ignored for now but need to reevaluate and restore later
    public void testStaleConsumers() throws Exception {
        final int numberOfConsumers = 5;
        final TimeDuration shortDuration = new TimeDuration(5L, TimeUnit.SECONDS);
        final StateNotificationConsumerBuilder builder = new InMemoryStateNotificationConsumerBuilderImpl(shortDuration,
                                                                                                          new TimeDuration(1L, TimeUnit.SECONDS));
        notificationService = new InMemoryStateNotificationServiceImpl(builder);
        final Queue<StateNotificationConsumerId> consumerIds = new LinkedBlockingQueue<>(trackAndRegisterConsumers(numberOfConsumers));
        final AtomicBoolean stopFlag = new AtomicBoolean(false);

        final Future<List<CurrentState<?, ?>>> resultsFromActiveConsumer = Executors.newFixedThreadPool(1).submit(createCallable(consumerIds.poll(),
                                                                                                                           notificationService,
                                                                                                                           new TimeDuration(1L, TimeUnit.SECONDS),
                                                                                                                           stopFlag));

        Thread.sleep(shortDuration.valueOf(TimeUnit.MILLISECONDS));
        final int numberOfNotifications = 200;
        final Set<Identifier<WebServer>> notifications = trackAndAddNotifications(numberOfNotifications);
        Thread.sleep(shortDuration.valueOf(TimeUnit.MILLISECONDS));

        stopFlag.set(true);

        for (final StateNotificationConsumerId consumerId : consumerIds) {
            assertTrue(notificationService.pollUpdatedStates(consumerId,
                                                             new TimeRemainingCalculator(new TimeDuration(200L, TimeUnit.MILLISECONDS))).isEmpty());
        }

        final List<CurrentState<?, ?>> activeResults = resultsFromActiveConsumer.get();

        assertEquals(numberOfNotifications,
                     activeResults.size());
        for (final CurrentState<?, ?> state : activeResults) {
            assertTrue(notifications.contains(state.getId()));
        }
    }

    private void setupStateServiceMock() {
        when(stateService.getCurrentState(Matchers.<Identifier<WebServer>>anyObject())).thenAnswer(new Answer<CurrentState<WebServer, WebServerReachableState>>() {
            @Override
            public CurrentState<WebServer, WebServerReachableState> answer(final InvocationOnMock invocation) throws Throwable {
                final Identifier<WebServer> webServer = (Identifier<WebServer>)invocation.getArguments()[0];
                return new CurrentState<>(webServer,
                                          WebServerReachableState.WS_REACHABLE,
                                          DateTime.now(),
                                          StateType.WEB_SERVER);
            }
        });
    }

    private Set<StateNotificationConsumerId> trackAndRegisterConsumers(final int aNumberOfConsumers) {
        final Set<StateNotificationConsumerId> consumerIds = new HashSet<>(aNumberOfConsumers);
        for (int i = 0; i < aNumberOfConsumers; i++) {
            consumerIds.add(notificationService.register());
        }
        return consumerIds;
    }

    private Set<Identifier<WebServer>> trackAndAddNotifications(final int aNumberOfNotifications) {
        final Set<Identifier<WebServer>> webServers = new HashSet<>(aNumberOfNotifications);
        for (int i = 0; i < aNumberOfNotifications; i++) {
            final Identifier<WebServer> webServerId = new Identifier<>((long) i);
            webServers.add(webServerId);
            notificationService.notifyStateUpdated(new CurrentState<>(webServerId,
                                                                      WebServerReachableState.WS_REACHABLE,
                                                                      DateTime.now(),
                                                                      StateType.WEB_SERVER));
        }
        return webServers;
    }

    private Callable<List<CurrentState<?, ?>>> createCallable(final StateNotificationConsumerId aConsumerId,
                                                                                            final StateNotificationService aService,
                                                                                            final TimeDuration aDuration,
                                                                                            final AtomicBoolean aStopFlag) {
        return new Callable<List<CurrentState<?, ?>>>() {
            @Override
            public List<CurrentState<?, ?>> call() throws Exception {
                final List<CurrentState<?, ?>> states = new ArrayList<>();
                while (!aStopFlag.get()) {
                    final List<CurrentState<?, ?>> polledStates = aService.pollUpdatedStates(aConsumerId,
                                                                                       new TimeRemainingCalculator(aDuration));
                    states.addAll(polledStates);
                }

                return states;
            }
        };
    }
}
