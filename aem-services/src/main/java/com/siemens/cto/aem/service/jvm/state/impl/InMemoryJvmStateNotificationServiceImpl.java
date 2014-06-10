package com.siemens.cto.aem.service.jvm.state.impl;

import java.util.concurrent.TimeUnit;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.state.AbstractStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumer;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateService;

public class InMemoryJvmStateNotificationServiceImpl extends AbstractStateNotificationService implements JvmStateNotificationService {

    private static final TimeDuration DEFAULT_INACTIVE_TIME = new TimeDuration(5L, TimeUnit.MINUTES);
    private final Stale stale;

    public InMemoryJvmStateNotificationServiceImpl(final JvmStateService theStateService) {
        this(theStateService,
             DEFAULT_INACTIVE_TIME);
    }

    InMemoryJvmStateNotificationServiceImpl(final JvmStateService theStateService,
                                            final TimeDuration theInactiveTime) {
        super();
        stale = new Stale(theInactiveTime);
    }

    @Override
    public void notifyJvmStateUpdated(final Identifier<Jvm> aJvmId) {
        notifyRegisteredConsumers(aJvmId);
    }

    @Override
    protected JvmStateNotificationConsumer createConsumer() {
        final JvmStateNotificationConsumer consumer = new InMemoryJvmStateNotificationConsumerImpl(stale);
        return consumer;
    }
}
