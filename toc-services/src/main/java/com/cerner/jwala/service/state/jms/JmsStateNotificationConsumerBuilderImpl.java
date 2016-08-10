package com.cerner.jwala.service.state.jms;

import com.cerner.jwala.common.time.Stale;
import com.cerner.jwala.common.time.TimeDuration;
import com.cerner.jwala.service.state.StateNotificationConsumer;
import com.cerner.jwala.service.state.StateNotificationConsumerBuilder;

import java.util.concurrent.TimeUnit;

public class JmsStateNotificationConsumerBuilderImpl implements StateNotificationConsumerBuilder {

    private static final TimeDuration DEFAULT_INACTIVE_TIME = new TimeDuration(3L, TimeUnit.MINUTES);
    private static final TimeDuration DEFAULT_POLL_TIME = new TimeDuration(30L, TimeUnit.SECONDS);

    private final JmsPackageBuilder jmsPackageBuilder;
    private final Stale stale;
    private final TimeDuration defaultPollTime;

    public JmsStateNotificationConsumerBuilderImpl(final JmsPackageBuilder theJmsPackageBuilder) {
        this(theJmsPackageBuilder,
             DEFAULT_INACTIVE_TIME,
             DEFAULT_POLL_TIME);
    }

    public JmsStateNotificationConsumerBuilderImpl(final JmsPackageBuilder theJmsPackageBuilder,
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
