package com.siemens.cto.aem.service.state.jms.sender.message;

import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;

public interface JmsMessageSerializer<T> extends MessageCreator {

    T extract(final Message aMessage) throws JMSException;

}
