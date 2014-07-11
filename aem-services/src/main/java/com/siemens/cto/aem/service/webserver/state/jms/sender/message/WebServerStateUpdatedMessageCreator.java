package com.siemens.cto.aem.service.webserver.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.springframework.jms.core.MessageCreator;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.message.StateMessageKey;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.state.jms.sender.message.StateUpdatedMessageCreator;

public class WebServerStateUpdatedMessageCreator extends StateUpdatedMessageCreator<Identifier<WebServer>> implements MessageCreator {

    public WebServerStateUpdatedMessageCreator(final Identifier<WebServer> theUpdatedThing) {
        super(theUpdatedThing);
    }

    @Override
    protected void fill(final MapMessage aMessage) throws JMSException {
        aMessage.setString(StateMessageKey.ID.getKey(), updatedThing.getId().toString());
    }
}
