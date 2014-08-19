package com.siemens.cto.aem.domain.model.group.command;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupStateRule;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;

public class SetGroupStateCommand extends SetStateCommand<Group, GroupState> implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    public SetGroupStateCommand(final Identifier<Group> theId,
            final GroupState theGroupState) {
            super(new CurrentGroupState(theId,  theGroupState, DateTime.now()));
    }

    public SetGroupStateCommand(final CurrentGroupState theGroupState) {
            super(theGroupState);
    }

    private GroupState getNewGroupState() {
        return super.getNewState().getState();
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new GroupIdRule(getNewState().getId()),
                                new GroupStateRule(getNewGroupState())).validate();
    }
}
