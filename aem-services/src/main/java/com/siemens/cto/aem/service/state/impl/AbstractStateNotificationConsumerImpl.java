package com.siemens.cto.aem.service.state.impl;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemaining;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;

public abstract class AbstractStateNotificationConsumerImpl<T> implements StateNotificationConsumer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStateNotificationConsumerImpl.class);

    private final Stale stale;
    private final TimeDuration defaultPollDuration;

    private volatile long lastAccessTime;
    private volatile boolean isClosed;

    protected AbstractStateNotificationConsumerImpl(final Stale theStale,
                                                    final TimeDuration theDefaultPollDuration,
                                                    final long theLastAccessTime) {
        stale = theStale;
        defaultPollDuration = theDefaultPollDuration;
        lastAccessTime = theLastAccessTime;
    }

    @Override
    public boolean isStale() {
        return stale.isStale(lastAccessTime);
    }

    @Override
    public synchronized void close() {
        if (!isClosed) {
            isClosed = true;
            closeHelper();
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public Set<T> getNotifications(final TimeRemainingCalculator aRequestedTimeoutCalculator) {
        updateLastAccessTime();

        final Set<T> notifications = new HashSet<>();

        TimeRemainingCalculator calculator = new TimeRemainingCalculator(defaultPollDuration);
        TimeRemaining timeRemaining;
        while ( (timeRemaining = calculator.getTimeRemaining()).isTimeRemaining()) {
            final T notification = getNotificationsHelper(timeRemaining.getDuration());
            if (notification != null) {
                notifications.add(notification);
                calculator = aRequestedTimeoutCalculator;
            }
        }

        return notifications;
    }

    protected void updateLastAccessTime() {
        updateLastAccessTime(System.currentTimeMillis());
    }

    protected void updateLastAccessTime(final long aLastAccessTime) {
        lastAccessTime = aLastAccessTime;
    }

    protected abstract void closeHelper();

    protected abstract T getNotificationsHelper(final TimeDuration someTimeLeft);
}
