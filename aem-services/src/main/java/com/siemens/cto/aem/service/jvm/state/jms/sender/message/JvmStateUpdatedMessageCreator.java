package com.siemens.cto.aem.service.jvm.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.MessageCreator;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class JvmStateUpdatedMessageCreator implements MessageCreator {

    private final Identifier<Jvm> updatedJvm;

    public JvmStateUpdatedMessageCreator(final Identifier<Jvm> theUpdatedJvm) {
        updatedJvm = theUpdatedJvm;
    }

    @Override
    public Message createMessage(final Session session) throws JMSException {

        final MapMessage message = session.createMapMessage();
        new JvmStateUpdatedMessageFiller(message).fill(updatedJvm);

        return message;
    }
}
