package com.siemens.cto.aem.domain.model.group.command;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupStateRule;

public class SetGroupStateCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final GroupState newGroupState;

    public SetGroupStateCommand(final Identifier<Group> theId,
                              final GroupState theGroupState) {
        id = theId;
        newGroupState = theGroupState;
    }

    public Identifier<Group> getId() {
        return id;
    }
    
    public GroupState getNewGroupState() {
        return newGroupState;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new GroupIdRule(id),
                                new GroupStateRule(newGroupState)).validate();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        SetGroupStateCommand rhs = (SetGroupStateCommand) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.newGroupState, rhs.newGroupState)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(newGroupState)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("newGroupState", newGroupState)
                .toString();
    }
}
