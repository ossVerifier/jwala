package com.siemens.cto.aem.service.webserver.state.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import com.siemens.cto.aem.common.time.Stale;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.jvm.state.jms.JmsPackage;
import com.siemens.cto.aem.service.state.StateNotificationConsumer;
import com.siemens.cto.aem.service.state.jms.JmsStateNotificationConsumerImpl;
import com.siemens.cto.aem.service.state.jms.sender.message.IdentifierStateUpdatedMessageExtractor;

public class WebServerJmsSateNotificationConsumerImpl extends JmsStateNotificationConsumerImpl<Identifier<WebServer>> implements StateNotificationConsumer<Identifier<WebServer>> {

    public WebServerJmsSateNotificationConsumerImpl(final JmsPackage theJmsPackage,
                                                    final Stale theStale,
                                                    final TimeDuration theDefaultPollDuration) {
        super(theJmsPackage,
              theStale,
              theDefaultPollDuration);
    }

    @Override
    protected Identifier<WebServer> getNoMessageRead() {
        return null;
    }

    @Override
    protected Identifier<WebServer> extractFromMessage(final Message aMessage) throws JMSException {
        return new IdentifierStateUpdatedMessageExtractor<WebServer>(aMessage).extract();
    }
}
