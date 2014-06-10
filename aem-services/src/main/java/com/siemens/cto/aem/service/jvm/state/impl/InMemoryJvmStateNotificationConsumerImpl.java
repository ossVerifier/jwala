package com.siemens.cto.aem.service.jvm.state.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.state.AbstractStateNotificationConsumerImpl;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumer;

public class InMemoryJvmStateNotificationConsumerImpl extends AbstractStateNotificationConsumerImpl implements JvmStateNotificationConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryJvmStateNotificationConsumerImpl.class);

    private static final int DEFAULT_CAPACITY = 1000;

    private final BlockingQueue<Identifier<Jvm>> jvmStates;

    public InMemoryJvmStateNotificationConsumerImpl(final Stale theStale) {
        this(theStale,
             DEFAULT_CAPACITY,
             System.currentTimeMillis());
    }

    public InMemoryJvmStateNotificationConsumerImpl(final Stale theStale,
                                                    final int theCapacity,
                                                    final long theLastAccessTime) {
        super(theStale,
              theLastAccessTime);
        jvmStates = new LinkedBlockingQueue<>(theCapacity);
    }

    @Override
    public void addNotification(final Identifier<Jvm> aJvmId) {
        if (!jvmStates.offer(aJvmId)) {
            LOGGER.warn("Notification queue is full");
        }
    }

    @Override
    protected void closeHelper() {
        jvmStates.clear();
    }

    @Override
    protected Identifier<Jvm> getNotificationsHelper(final TimeDuration someTimeLeft) {
        try {
            return jvmStates.poll(someTimeLeft.valueOf(), someTimeLeft.getUnit());
        } catch (final InterruptedException ie) {
            LOGGER.info("Interrupted while retrieving notifications", ie);
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
