package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.core.MessageCreator;

public interface JmsMessageSerializer<T> extends MessageCreator {

    T extract(final Message aMessage) throws JMSException;

}
