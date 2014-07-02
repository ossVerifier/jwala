package com.siemens.cto.aem.service.jvm.state;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.common.time.TimeRemaining;
import com.siemens.cto.aem.common.time.TimeRemainingCalculator;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public abstract class AbstractStateNotificationConsumerImpl implements JvmStateNotificationConsumer {

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
    public Set<Identifier<Jvm>> getNotifications(final TimeRemainingCalculator aRequestedTimeoutCalculator) {
        updateLastAccessTime();

        final Set<Identifier<Jvm>> jvms = new HashSet<>();

        TimeRemainingCalculator calculator = new TimeRemainingCalculator(defaultPollDuration);
        TimeRemaining timeRemaining;
        while ( (timeRemaining = calculator.getTimeRemaining()).isTimeRemaining()) {
            final Identifier<Jvm> jvm = getNotificationsHelper(timeRemaining.getDuration());
            if (jvm != null) {
                jvms.add(jvm);
                calculator = aRequestedTimeoutCalculator;
            }
        }

        return jvms;
    }

    protected void updateLastAccessTime() {
        updateLastAccessTime(System.currentTimeMillis());
    }

    protected void updateLastAccessTime(final long aLastAccessTime) {
        lastAccessTime = aLastAccessTime;
    }

    protected abstract void closeHelper();

    protected abstract Identifier<Jvm> getNotificationsHelper(final TimeDuration someTimeLeft);
}
