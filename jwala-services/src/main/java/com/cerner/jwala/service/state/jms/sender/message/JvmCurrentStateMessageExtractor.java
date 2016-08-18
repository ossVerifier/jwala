package com.cerner.jwala.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.state.message.CommonStateKey;

public class JvmCurrentStateMessageExtractor extends AbstractCurrentStateMessageExtractor<JvmState> implements CurrentStateMessageExtractor {

    public JvmCurrentStateMessageExtractor() {
        super(StateType.JVM);
    }

    @Override
    JvmState getState(final MapMessage aMessage) throws JMSException {
        return JvmState.convertFrom(aMessage.getString(CommonStateKey.STATE.getKey()));
    }
}
