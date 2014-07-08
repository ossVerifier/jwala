package com.siemens.cto.aem.service.jvm.state.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.Destination;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.jvm.state.AbstractStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationConsumer;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.jms.sender.message.JvmStateUpdatedMessageCreator;

public class JmsJvmStateNotificationServiceImpl extends AbstractStateNotificationService implements JvmStateNotificationService {

    private static final TimeDuration DEFAULT_INACTIVE_TIME = new TimeDuration(3L,
                                                                               TimeUnit.MINUTES);
    private static final TimeDuration DEFAULT_POLL_TIME = new TimeDuration(30L,
                                                                           TimeUnit.SECONDS);

    private final JmsPackageBuilder builder;
    private final JmsTemplate template;
    private final Destination destination;
    private final Stale stale;
    private final TimeDuration defaultPollTime;

    public JmsJvmStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
                                              final JmsTemplate theTemplate,
                                              final Destination theDestination) {
        this(theBuilder,
             theTemplate,
             theDestination,
             DEFAULT_INACTIVE_TIME,
             DEFAULT_POLL_TIME);
    }

    public JmsJvmStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
                                              final JmsTemplate theTemplate,
                                              final Destination theDestination,
                                              final TimeDuration theInactiveTime,
                                              final TimeDuration theDefaultPollTime) {
        super();
        builder = theBuilder;
        template = theTemplate;
        destination = theDestination;
        stale = new Stale(theInactiveTime);
        defaultPollTime = theDefaultPollTime;
    }

    @Override
    @Transactional
    public void notifyJvmStateUpdated(final Identifier<Jvm> aJvmId) {
        prune();
        template.send(destination,
                      new JvmStateUpdatedMessageCreator(aJvmId));
    }

    @Override
    protected JvmStateNotificationConsumer createConsumer() {
        final JvmStateNotificationConsumer consumer = new JmsJvmStateNotificationConsumerImpl(builder.build(),
                                                                                              stale,
                                                                                              defaultPollTime);
        return consumer;
    }
}
