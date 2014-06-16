package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupNameRule;

public class UpdateGroupCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String newName;

    public UpdateGroupCommand(final Identifier<Group> theId,
                              final String theNewName) {
        id = theId;
        newName = theNewName;
    }

    public Identifier<Group> getId() {
        return id;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new GroupIdRule(id),
                                new GroupNameRule(newName)).validate();
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
        UpdateGroupCommand rhs = (UpdateGroupCommand) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.newName, rhs.newName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(newName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("newName", newName)
                .toString();
    }
}
