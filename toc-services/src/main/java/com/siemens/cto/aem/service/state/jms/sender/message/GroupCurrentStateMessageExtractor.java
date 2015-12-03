package com.siemens.cto.aem.service.state.jms.sender.message;

import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;

import javax.jms.JMSException;
import javax.jms.MapMessage;

public class GroupCurrentStateMessageExtractor extends AbstractCurrentStateMessageExtractor<GroupState> implements CurrentStateMessageExtractor {

    public GroupCurrentStateMessageExtractor() {
        super(StateType.GROUP);
    }

    @Override
    GroupState getState(final MapMessage aMessage) throws JMSException {
        return GroupState.convertFrom(aMessage.getString(CommonStateKey.STATE.getKey()));
    }
}
