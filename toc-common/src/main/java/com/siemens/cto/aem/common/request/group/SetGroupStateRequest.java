package com.siemens.cto.aem.common.request.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.group.GroupIdRule;
import com.siemens.cto.aem.common.rule.group.GroupStateRule;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import org.joda.time.DateTime;

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
