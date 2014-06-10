package com.siemens.cto.aem.service.jvm.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessageKey;

public class JvmStateUpdatedMessageExtractor {

    private final MapMessage source;

    public JvmStateUpdatedMessageExtractor(final Message theSource) throws JMSException {
        if (theSource instanceof MapMessage) {
            source = (MapMessage)theSource;
        } else {
            throw new JMSException("Unsupported JMS message type :" + theSource.getClass() + " with message : {" + theSource.toString() + "}");
        }
    }

    public Identifier<Jvm> extractId() throws JMSException {
        final String rawId = source.getString(JvmStateMessageKey.JVM_ID.getKey());
        return new Identifier<>(rawId);
    }
}
