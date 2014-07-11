package com.siemens.cto.aem.service.state.jms;

import java.util.concurrent.TimeUnit;

import javax.jms.Destination;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.service.jvm.state.jms.JmsPackageBuilder;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.impl.AbstractStateNotificationService;

public abstract class JmsStateNotificationServiceImpl<S> extends AbstractStateNotificationService<S> implements StateNotificationService<S> {

    private static final TimeDuration DEFAULT_INACTIVE_TIME = new TimeDuration(3L,
                                                                               TimeUnit.MINUTES);
    private static final TimeDuration DEFAULT_POLL_TIME = new TimeDuration(30L,
                                                                           TimeUnit.SECONDS);

    protected final JmsPackageBuilder builder;
    protected final JmsTemplate template;
    protected final Destination destination;
    protected final Stale stale;
    protected final TimeDuration defaultPollTime;

    public JmsStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
                                           final JmsTemplate theTemplate,
                                           final Destination theDestination) {
        this(theBuilder,
             theTemplate,
             theDestination,
             DEFAULT_INACTIVE_TIME,
             DEFAULT_POLL_TIME);
    }

    public JmsStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
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
    public void notifyStateUpdated(final Identifier<S> anUpdatedThing) {
        prune();
        template.send(destination,
                      constructCreator(anUpdatedThing));
    }

    protected abstract MessageCreator constructCreator(final Identifier<S> anUpdatedThing);
}
