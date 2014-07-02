package com.siemens.cto.aem.service.jvm.state.impl;

import java.util.concurrent.TimeUnit;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.state.AbstractStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumer;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;

public class InMemoryJvmStateNotificationServiceImpl extends AbstractStateNotificationService implements JvmStateNotificationService {

    private static final TimeDuration DEFAULT_INACTIVE_TIME = new TimeDuration(5L,
                                                                               TimeUnit.MINUTES);
    private static final TimeDuration DEFAULT_POLL_TIME = new TimeDuration(30L,
                                                                           TimeUnit.SECONDS);
    private final Stale stale;
    private final TimeDuration defaultPollTime;

    public InMemoryJvmStateNotificationServiceImpl() {
        this(DEFAULT_INACTIVE_TIME,
             DEFAULT_POLL_TIME);
    }

    InMemoryJvmStateNotificationServiceImpl(final TimeDuration theInactiveTime,
                                            final TimeDuration theDefaultPollTime) {
        super();
        stale = new Stale(theInactiveTime);
        defaultPollTime = theDefaultPollTime;
    }

    @Override
    public void notifyJvmStateUpdated(final Identifier<Jvm> aJvmId) {
        notifyRegisteredConsumers(aJvmId);
    }

    @Override
    protected JvmStateNotificationConsumer createConsumer() {
        final JvmStateNotificationConsumer consumer = new InMemoryJvmStateNotificationConsumerImpl(stale,
                                                                                                   defaultPollTime);
        return consumer;
    }
}
