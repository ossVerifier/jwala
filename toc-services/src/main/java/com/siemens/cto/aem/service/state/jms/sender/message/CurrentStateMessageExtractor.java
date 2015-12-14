package com.siemens.cto.aem.service.state.jms.sender.message;

import com.siemens.cto.aem.common.domain.model.state.CurrentState;

import javax.jms.JMSException;
import javax.jms.MapMessage;

public interface CurrentStateMessageExtractor {

    CurrentState extract(final MapMessage aMessage) throws JMSException;
}
