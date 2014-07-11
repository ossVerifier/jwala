package com.siemens.cto.aem.service.webserver.state.jms;

import javax.jms.Destination;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.jvm.state.jms.JmsPackageBuilder;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationServiceImpl;
import com.siemens.cto.aem.service.webserver.state.jms.sender.message.WebServerStateUpdatedMessageCreator;

public class WebServerJmsStateNotificationServiceImpl extends JmsStateNotificationServiceImpl<WebServer> implements StateNotificationService<WebServer> {

    public WebServerJmsStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
                                                    final JmsTemplate theTemplate,
                                                    final Destination theDestination) {
        super(theBuilder,
              theTemplate,
              theDestination);
    }

    public WebServerJmsStateNotificationServiceImpl(final JmsPackageBuilder theBuilder,
                                                    final JmsTemplate theTemplate,
                                                    final Destination theDestination,
                                                    final TimeDuration theInactiveTime,
                                                    final TimeDuration theDefaultPollTime) {
        super(theBuilder,
              theTemplate,
              theDestination,
              theInactiveTime,
              theDefaultPollTime);
    }

    @Override
    protected MessageCreator constructCreator(final Identifier<WebServer> anUpdatedThing) {
        return new WebServerStateUpdatedMessageCreator(anUpdatedThing);
    }

    @Override
    protected StateNotificationConsumer<Identifier<WebServer>> createConsumer() {
        return new WebServerJmsSateNotificationConsumerImpl(builder.build(),
                                                            stale,
                                                            defaultPollTime);
    }
}
