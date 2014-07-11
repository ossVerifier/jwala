package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

public abstract class StateUpdatedMessageExtractor<T> {

    protected final MapMessage source;

    public StateUpdatedMessageExtractor(final Message theSource) throws JMSException {
        if (theSource instanceof MapMessage) {
            source = (MapMessage)theSource;
        } else {
            throw new JMSException("Unsupported JMS message type :" + theSource.getClass() + " with message : {" + theSource.toString() + "}");
        }
    }

    public abstract T extract() throws JMSException;
}
