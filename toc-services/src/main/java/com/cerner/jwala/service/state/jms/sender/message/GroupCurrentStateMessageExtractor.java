package com.cerner.jwala.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.cerner.jwala.common.domain.model.group.GroupState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.state.message.CommonStateKey;

public class GroupCurrentStateMessageExtractor extends AbstractCurrentStateMessageExtractor<GroupState> implements CurrentStateMessageExtractor {

    public GroupCurrentStateMessageExtractor() {
        super(StateType.GROUP);
    }

    @Override
    GroupState getState(final MapMessage aMessage) throws JMSException {
        return GroupState.convertFrom(aMessage.getString(CommonStateKey.STATE.getKey()));
    }
}
