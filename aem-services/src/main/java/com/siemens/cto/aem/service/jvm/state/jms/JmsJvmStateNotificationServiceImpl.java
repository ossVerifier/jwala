package com.siemens.cto.aem.service.jvm.state.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.Destination;

import org.springframework.jms.core.JmsTemplate;

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

    private final JmsPackageBuilder builder;
    private final JmsTemplate template;
    private final Destination destination;
    private final Stale stale;

    public JmsJvmStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
                                              final JmsTemplate theTemplate,
                                              final Destination theDestination) {
        this(theBuilder,
             theTemplate,
             theDestination,
             DEFAULT_INACTIVE_TIME);
    }

    public JmsJvmStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
                                              final JmsTemplate theTemplate,
                                              final Destination theDestination,
                                              final TimeDuration theInactiveTime) {
        super();
        builder = theBuilder;
        template = theTemplate;
        destination = theDestination;
        stale = new Stale(theInactiveTime);
    }

    @Override
    public void notifyJvmStateUpdated(final Identifier<Jvm> aJvmId) {
        prune();
        template.send(destination,
                      new JvmStateUpdatedMessageCreator(aJvmId));
    }

    @Override
    protected JvmStateNotificationConsumer createConsumer() {
        final JvmStateNotificationConsumer consumer = new JmsJvmStateNotificationConsumerImpl(builder.build(),
                                                                                              stale);
        return consumer;
    }
}
