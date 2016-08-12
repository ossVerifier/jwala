package com.cerner.jwala.common.request.group;

import org.joda.time.DateTime;

import com.cerner.jwala.common.domain.model.group.CurrentGroupState;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupState;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.request.state.SetStateRequest;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.group.GroupIdRule;
import com.cerner.jwala.common.rule.group.GroupStateRule;

import java.io.Serializable;

public class SetGroupStateRequest extends SetStateRequest<Group, GroupState> implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    public SetGroupStateRequest(final Identifier<Group> theId,
                                final GroupState theGroupState) {
            super(new CurrentGroupState(theId,  theGroupState, DateTime.now()));
    }

    public SetGroupStateRequest(final CurrentGroupState theGroupState) {
            super(theGroupState);
    }

    private GroupState getNewGroupState() {
        return super.getNewState().getState();
    }

    @Override
    public void validate() throws BadRequestException {
        new MultipleRules(new GroupIdRule(getNewState().getId()),
                                new GroupStateRule(getNewGroupState())).validate();
    }
}
