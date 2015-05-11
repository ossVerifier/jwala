package com.siemens.cto.aem.service.state.impl;

import java.util.ArrayList;
import java.util.List;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemaining;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;

public abstract class AbstractStateNotificationConsumerImpl implements StateNotificationConsumer {

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
    public CurrentState<?,?> blockingGetNotification() {
        updateLastAccessTime();
        
        return getNotificationsHelper(defaultPollDuration);
    }

    @Override
    public List<CurrentState<?,?>> getNotifications(final TimeRemainingCalculator aRequestedTimeoutCalculator) {
        updateLastAccessTime();

        final List<CurrentState<?,?>> notifications = new ArrayList<>();

        TimeRemainingCalculator calculator = new TimeRemainingCalculator(defaultPollDuration);
        TimeRemaining timeRemaining;
        while ( (timeRemaining = calculator.getTimeRemaining()).isTimeRemaining()) {
            final CurrentState<?,?> notification = getNotificationsHelper(timeRemaining.getDuration());
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

    protected abstract CurrentState<?,?> getNotificationsHelper(final TimeDuration someTimeLeft);
}
