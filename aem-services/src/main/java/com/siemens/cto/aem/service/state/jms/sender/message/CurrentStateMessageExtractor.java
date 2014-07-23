package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.siemens.cto.aem.domain.model.state.CurrentState;

public interface CurrentStateMessageExtractor<T extends CurrentState<?,?>> {

    T extract(final MapMessage aMessage) throws JMSException;
}
