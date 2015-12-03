package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class InMemoryStateNotificationConsumerImpl extends AbstractStateNotificationConsumerImpl implements StateNotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryStateNotificationConsumerImpl.class);

    private static final int DEFAULT_CAPACITY = 1000;

    private final BlockingQueue<CurrentState> notifications;

    public InMemoryStateNotificationConsumerImpl(final Stale theStale,
                                                 final TimeDuration theDefaultPollDuration) {
        this(theStale,
             theDefaultPollDuration,
             DEFAULT_CAPACITY,
             System.currentTimeMillis());
    }

    public InMemoryStateNotificationConsumerImpl(final Stale theStale,
                                                 final TimeDuration theDefaultPollDuration,
                                                 final int theCapacity,
                                                 final long theLastAccessTime) {
        super(theStale,
              theDefaultPollDuration,
              theLastAccessTime);
        notifications = new LinkedBlockingQueue<>(theCapacity);
    }

    @Override
    public void addNotification(final CurrentState aNotification) {
        if (!notifications.offer(aNotification)) {
            LOGGER.warn("Notification queue is full");
        }
    }

    @Override
    protected void closeHelper() {
        notifications.clear();
    }

    @Override
    protected CurrentState getNotificationsHelper(final TimeDuration someTimeLeft) {
        try {
            return notifications.poll(someTimeLeft.valueOf(), someTimeLeft.getUnit());
        } catch (final InterruptedException ie) {
            LOGGER.info("Interrupted while retrieving notifications", ie);
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
