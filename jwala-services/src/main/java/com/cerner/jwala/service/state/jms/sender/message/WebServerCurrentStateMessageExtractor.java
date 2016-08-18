package com.cerner.jwala.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.state.message.CommonStateKey;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;

public class WebServerCurrentStateMessageExtractor extends AbstractCurrentStateMessageExtractor<WebServerReachableState> {

    public WebServerCurrentStateMessageExtractor() {
        super(StateType.WEB_SERVER);
    }

    @Override
    WebServerReachableState getState(final MapMessage aMessage) throws JMSException {
        return WebServerReachableState.convertFrom(aMessage.getString(CommonStateKey.STATE.getKey()));
    }
}
