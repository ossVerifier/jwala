package com.siemens.cto.aem.service.jvm.state.impl;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class InMemoryJvmStateNotificationConsumerImplTest {

    private InMemoryJvmStateNotificationConsumerImpl consumer;
    private Stale stale;
    private TimeDuration pollDuration;

    @Before
    public void setUp() throws Exception {
        stale = new Stale(new TimeDuration(5L,
                                           TimeUnit.MINUTES));
        pollDuration = new TimeDuration(1L,
                                        TimeUnit.SECONDS);
        consumer = new InMemoryJvmStateNotificationConsumerImpl(stale,
                                                                pollDuration);
    }

    @Test
    public void testAddNotifications() {
        final int numberOfNotifications = 500;

        addNotifications(numberOfNotifications);

        final Set<Identifier<Jvm>> notifications = consumer.getNotifications(getLongAmountOfTime());
        assertEquals(numberOfNotifications,
                     notifications.size());

        final Set<Identifier<Jvm>> additionalNotifications = consumer.getNotifications(getShortAmountOfTime());
        assertTrue(additionalNotifications.isEmpty());
    }

    @Test
    public void testNoNotificationsAdded() {
        final TimeRemainingCalculator shortTimeRemaining = spy(getShortAmountOfTime());
        final Set<Identifier<Jvm>> notifications = consumer.getNotifications(shortTimeRemaining);
        assertTrue(notifications.isEmpty());
        verify(shortTimeRemaining, never()).getTimeRemaining();
    }

    @Test
    public void testAddTooManyNotifications() {
        final int maxCapacity = 7;
        final JvmStateNotificationConsumer smallConsumer = new InMemoryJvmStateNotificationConsumerImpl(stale,
                                                                                                        pollDuration,
                                                                                                        maxCapacity,
                                                                                                        System.currentTimeMillis());
        addNotifications(smallConsumer,
                         maxCapacity + 1);
        final Set<Identifier<Jvm>> notifications = smallConsumer.getNotifications(getShortAmountOfTime());

        assertEquals(maxCapacity,
                     notifications.size());
    }

    @Test
    public void testClose() {
        final int numberOfNotifications = 500;

        addNotifications(numberOfNotifications);

        consumer.close();

        assertTrue(consumer.isClosed());
        assertTrue(consumer.getNotifications(new TimeRemainingCalculator(new TimeDuration(200L, TimeUnit.MILLISECONDS))).isEmpty());
    }

    @Test
    public void testInterrupted() throws Exception {
        final AtomicReference<Identifier<Jvm>> result = new AtomicReference<>(new Identifier<Jvm>(123456L));
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);
        final TimeDuration waitPeriod = new TimeDuration(10L, TimeUnit.SECONDS);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                start.countDown();
                final Identifier<Jvm> jvm = consumer.getNotificationsHelper(waitPeriod);
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
        addNotifications(consumer,
                         aNumberOfNotifications);
    }

    private void addNotifications(final JvmStateNotificationConsumer aConsumer,
                                  final int aNumberOfNotifications) {
        for (int i = 0; i < aNumberOfNotifications; i++) {
            aConsumer.addNotification(new Identifier<Jvm>((long)i));
        }
    }

    private TimeRemainingCalculator getShortAmountOfTime() {
        return createCalculator(200L, TimeUnit.MILLISECONDS);
    }

    private TimeRemainingCalculator getLongAmountOfTime() {
        return createCalculator(5L, TimeUnit.SECONDS);
    }

    private TimeRemainingCalculator createCalculator(final long aTime,
                                                     final TimeUnit aUnit) {
        return new TimeRemainingCalculator(new TimeDuration(aTime,
                                                            aUnit));
    }
}
