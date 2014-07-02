package com.siemens.cto.aem.service.jvm.state.impl;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumerId;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryJvmStateNotificationServiceImplTest {

    private JvmStateNotificationService service;
    private JvmStateService stateService;

    @Before
    public void setUp() throws Exception {
        setupStateServiceMock();
        service = new InMemoryJvmStateNotificationServiceImpl(new TimeDuration(5L,
                                                                               TimeUnit.MINUTES),
                                                              new TimeDuration(30L,
                                                                               TimeUnit.SECONDS));
    }

    @Test
    public void testNotifyJvmStateUpdated() throws Exception {
        final int numberOfConsumers = 5;
        final Set<JvmStateNotificationConsumerId> consumerIds = trackAndRegisterConsumers(numberOfConsumers);

        final int numberOfNotifications = 200;
        final Set<Identifier<Jvm>> jvms = trackAndAddNotifications(numberOfNotifications);

        for (final JvmStateNotificationConsumerId consumerId : consumerIds) {
            final Set<Identifier<Jvm>> currentJvmStates = service.pollUpdatedStates(consumerId,
                                                                                    new TimeRemainingCalculator(new TimeDuration(1L,
                                                                                                                                 TimeUnit.SECONDS)));
            assertEquals(numberOfNotifications,
                         currentJvmStates.size());
            for (final Identifier<Jvm> jvmId : currentJvmStates) {
                assertTrue(jvms.contains(jvmId));
            }
        }
    }

    @Test
    public void testDeregister() throws Exception {
        final int numberOfConsumers = 5;
        final Set<JvmStateNotificationConsumerId> consumerIds = trackAndRegisterConsumers(numberOfConsumers);

        final int numberOfNotifications = 200;
        trackAndAddNotifications(numberOfNotifications);

        for (final JvmStateNotificationConsumerId consumerId : consumerIds) {
            service.deregister(consumerId);
            final Set<Identifier<Jvm>> currentJvmStates = service.pollUpdatedStates(consumerId,
                                                                                    new TimeRemainingCalculator(new TimeDuration(500L,
                                                                                                                                 TimeUnit.MILLISECONDS)));
            assertTrue(currentJvmStates.isEmpty());
            assertFalse(service.isValid(consumerId));
        }
    }

    @Test
    public void testStaleConsumers() throws Exception {
        final int numberOfConsumers = 5;
        final TimeDuration shortDuration = new TimeDuration(5L,
                                                            TimeUnit.SECONDS);
        service = new InMemoryJvmStateNotificationServiceImpl(shortDuration,
                                                              new TimeDuration(1L,
                                                                               TimeUnit.SECONDS));
        final Queue<JvmStateNotificationConsumerId> consumerIds = new LinkedBlockingQueue<>(trackAndRegisterConsumers(numberOfConsumers));
        final AtomicBoolean stopFlag = new AtomicBoolean(false);

        final Future<Set<Identifier<Jvm>>> resultsFromActiveConsumer = Executors.newFixedThreadPool(1).submit(createCallable(consumerIds.poll(),
                                                                                                                             service,
                                                                                                                             new TimeDuration(1L,
                                                                                                                                              TimeUnit.SECONDS),
                                                                                                                             stopFlag));

        Thread.sleep(shortDuration.valueOf(TimeUnit.MILLISECONDS));
        final int numberOfNotifications = 200;
        final Set<Identifier<Jvm>> notifications = trackAndAddNotifications(numberOfNotifications);
        Thread.sleep(shortDuration.valueOf(TimeUnit.MILLISECONDS));

        stopFlag.set(true);

        for (final JvmStateNotificationConsumerId consumerId : consumerIds) {
            assertTrue(service.pollUpdatedStates(consumerId,
                                                 new TimeRemainingCalculator(new TimeDuration(200L,
                                                                                              TimeUnit.MILLISECONDS))).isEmpty());
        }

        final Set<Identifier<Jvm>> activeResults = resultsFromActiveConsumer.get();

        assertEquals(numberOfNotifications,
                     activeResults.size());
        for (final Identifier<Jvm> jvmId : activeResults) {
            assertTrue(notifications.contains(jvmId));
        }
    }

    private void setupStateServiceMock() {
        stateService = mock(JvmStateService.class);
        when(stateService.getCurrentJvmState(Matchers.<Identifier<Jvm>>anyObject())).thenAnswer(new Answer<CurrentJvmState>() {
            @Override
            public CurrentJvmState answer(final InvocationOnMock invocation) throws Throwable {
                final Identifier<Jvm> jvm = (Identifier<Jvm>)invocation.getArguments()[0];
                return new CurrentJvmState(jvm,
                                           JvmState.STARTED,
                                           DateTime.now());
            }
        });
    }

    private Set<JvmStateNotificationConsumerId> trackAndRegisterConsumers(final int aNumberOfConsumers) {
        final Set<JvmStateNotificationConsumerId> consumerIds = new HashSet<>(aNumberOfConsumers);
        for (int i = 0; i < aNumberOfConsumers; i++) {
            consumerIds.add(service.register());
        }
        return consumerIds;
    }

    private Set<Identifier<Jvm>> trackAndAddNotifications(final int aNumberOfNotifications) {
        final Set<Identifier<Jvm>> jvms = new HashSet<>(aNumberOfNotifications);
        for (int i = 0; i < aNumberOfNotifications; i++) {
            final Identifier<Jvm> jvmId = new Identifier<>((long) i);
            jvms.add(jvmId);
            service.notifyJvmStateUpdated(jvmId);
        }
        return jvms;
    }

    private Callable<Set<Identifier<Jvm>>> createCallable(final JvmStateNotificationConsumerId aConsumerId,
                                                          final JvmStateNotificationService aService,
                                                          final TimeDuration aDuration,
                                                          final AtomicBoolean aStopFlag) {
        return new Callable<Set<Identifier<Jvm>>>() {
            @Override
            public Set<Identifier<Jvm>> call() throws Exception {
                final Set<Identifier<Jvm>> states = new HashSet<>();
                while (!aStopFlag.get()) {
                    final Set<Identifier<Jvm>> polledStates = aService.pollUpdatedStates(aConsumerId,
                                                                                         new TimeRemainingCalculator(aDuration));
                    states.addAll(polledStates);
                }

                return states;
            }
        };
    }
}
