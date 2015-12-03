package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;

import java.util.concurrent.TimeUnit;

public class InMemoryStateNotificationConsumerBuilderImpl implements StateNotificationConsumerBuilder {

    private static final TimeDuration DEFAULT_INACTIVE_TIME = new TimeDuration(3L,
                                                                               TimeUnit.MINUTES);
    private static final TimeDuration DEFAULT_POLL_TIME = new TimeDuration(30L,
                                                                           TimeUnit.SECONDS);

    private final Stale stale;
    private final TimeDuration defaultPollTime;

    public InMemoryStateNotificationConsumerBuilderImpl() {
        this(DEFAULT_INACTIVE_TIME,
             DEFAULT_POLL_TIME);
    }

    public InMemoryStateNotificationConsumerBuilderImpl(final TimeDuration theInactiveTime,
                                                        final TimeDuration theDefaultPollTime) {
        stale = new Stale(theInactiveTime);
        defaultPollTime = theDefaultPollTime;
    }

    @Override
    public StateNotificationConsumer build() {
        return new InMemoryStateNotificationConsumerImpl(stale,
                                                         defaultPollTime);
    }
}
