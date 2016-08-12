package com.cerner.jwala.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.cerner.jwala.common.domain.model.state.CurrentState;

public interface CurrentStateMessageExtractor {

    CurrentState extract(final MapMessage aMessage) throws JMSException;
}
