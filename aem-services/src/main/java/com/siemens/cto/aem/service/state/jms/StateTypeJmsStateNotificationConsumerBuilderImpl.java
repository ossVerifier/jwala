package com.siemens.cto.aem.service.state.jms;

import java.util.concurrent.TimeUnit;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.StateNotificationConsumerBuilder;

public class StateTypeJmsStateNotificationConsumerBuilderImpl implements StateNotificationConsumerBuilder {

    private static final TimeDuration DEFAULT_INACTIVE_TIME = new TimeDuration(3L,
                                                                               TimeUnit.MINUTES);
    private static final TimeDuration DEFAULT_POLL_TIME = new TimeDuration(30L,
                                                                           TimeUnit.SECONDS);

    private final JmsPackageBuilder jmsPackageBuilder;
    private final Stale stale;
    private final TimeDuration defaultPollTime;

    public StateTypeJmsStateNotificationConsumerBuilderImpl(final JmsPackageBuilder theJmsPackageBuilder) {
        this(theJmsPackageBuilder,
             DEFAULT_INACTIVE_TIME,
             DEFAULT_POLL_TIME);
    }

    public StateTypeJmsStateNotificationConsumerBuilderImpl(final JmsPackageBuilder theJmsPackageBuilder,
                                                            final TimeDuration theInactiveTime,
                                                            final TimeDuration theDefaultPollTime) {
        jmsPackageBuilder = theJmsPackageBuilder;
        stale = new Stale(theInactiveTime);
        defaultPollTime = theDefaultPollTime;
    }

    @Override
    public StateNotificationConsumer build() {
        return new JmsStateNotificationConsumerImpl(jmsPackageBuilder.build(),
                                                    stale,
                                                    defaultPollTime);
    }
}
