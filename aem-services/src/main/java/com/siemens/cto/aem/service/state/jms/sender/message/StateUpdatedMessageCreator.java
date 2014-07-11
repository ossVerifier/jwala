package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.MessageCreator;

public abstract class StateUpdatedMessageCreator<T> implements MessageCreator {

    protected final T updatedThing;

    public StateUpdatedMessageCreator(final T theUpdatedThing) {
        updatedThing = theUpdatedThing;
    }

    @Override
    public Message createMessage(final Session session) throws JMSException {

        final MapMessage message = session.createMapMessage();
        fill(message);
        return message;
    }

    protected abstract void fill(final MapMessage aMessage) throws JMSException;
}
