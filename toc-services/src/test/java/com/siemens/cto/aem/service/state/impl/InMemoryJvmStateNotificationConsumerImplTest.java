package com.siemens.cto.aem.service.state.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.JMSException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;

public class InMemoryJvmStateNotificationConsumerImplTest {

    private InMemoryStateNotificationConsumerImpl consumer;
    private Stale stale;
    private TimeDuration pollDuration;

    @Before
    public void setUp() throws Exception {
        stale = new Stale(new TimeDuration(5L, TimeUnit.MINUTES));
        pollDuration = new TimeDuration(1L, TimeUnit.SECONDS);
        consumer = new InMemoryStateNotificationConsumerImpl(stale, pollDuration);
    }

    @Test
    public void testAddNotifications() throws JMSException {
        final int numberOfNotifications = 500;

        addNotifications(numberOfNotifications);

        final List<CurrentState<?, ?>> notifications = consumer.getNotifications(getLongAmountOfTime());
        assertEquals(numberOfNotifications, notifications.size());

        final List<CurrentState<?, ?>> additionalNotifications = consumer.getNotifications(getShortAmountOfTime());
        assertTrue(additionalNotifications.isEmpty());
    }

    @Test
    public void testNoNotificationsAdded() throws JMSException {
        final TimeRemainingCalculator shortTimeRemaining = spy(getShortAmountOfTime());
        final List<CurrentState<?, ?>> notifications = consumer.getNotifications(shortTimeRemaining);
        assertTrue(notifications.isEmpty());
        verify(shortTimeRemaining, never()).getTimeRemaining();
    }

    @Test
    public void testAddTooManyNotifications() throws JMSException {
        final int maxCapacity = 7;
        final StateNotificationConsumer smallConsumer =
                new InMemoryStateNotificationConsumerImpl(stale, pollDuration, maxCapacity, System.currentTimeMillis());
        addNotifications(smallConsumer, maxCapacity + 1);
        final List<CurrentState<?, ?>> notifications = smallConsumer.getNotifications(getShortAmountOfTime());

        assertEquals(maxCapacity, notifications.size());
    }

    @Test
    public void testClose() throws JMSException {
        final int numberOfNotifications = 500;

        addNotifications(numberOfNotifications);

        consumer.close();

        assertTrue(consumer.isClosed());
        assertTrue(consumer.getNotifications(
                new TimeRemainingCalculator(new TimeDuration(200L, TimeUnit.MILLISECONDS))).isEmpty());
    }

    @Test
    public void testInterrupted() throws Exception {
        final AtomicReference<CurrentState<?, ?>> result =
                new AtomicReference<CurrentState<?, ?>>(new CurrentState<>(new Identifier<Jvm>(123456L),
                        JvmState.JVM_STOPPED, DateTime.now(), StateType.JVM));
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final TimeDuration waitPeriod = new TimeDuration(10L, TimeUnit.SECONDS);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                start.countDown();
                final CurrentState<?, ?> jvm = consumer.getNotificationsHelper(waitPeriod);
                result.set(jvm);
                end.countDown();
            }
        });

        thread.start();
        start.await(waitPeriod.valueOf(), waitPeriod.getUnit());
        Thread.sleep(TimeUnit.MILLISECONDS.toMillis(500));
        thread.interrupt();
        end.await(waitPeriod.valueOf(), waitPeriod.getUnit());

        assertNull(result.get());
    }

    private void addNotifications(final int aNumberOfNotifications) {
        addNotifications(consumer, aNumberOfNotifications);
    }

    private void addNotifications(final StateNotificationConsumer aConsumer, final int aNumberOfNotifications) {
        for (int i = 0; i < aNumberOfNotifications; i++) {
            aConsumer.addNotification(new CurrentState<>(new Identifier<Jvm>((long) i), JvmState.JVM_STARTED, DateTime
                    .now(), StateType.JVM));
        }
    }

    private TimeRemainingCalculator getShortAmountOfTime() {
        return createCalculator(200L, TimeUnit.MILLISECONDS);
    }

    private TimeRemainingCalculator getLongAmountOfTime() {
        return createCalculator(5L, TimeUnit.SECONDS);
    }

    private TimeRemainingCalculator createCalculator(final long aTime, final TimeUnit aUnit) {
        return new TimeRemainingCalculator(new TimeDuration(aTime, aUnit));
    }
}
