package com.siemens.cto.aem.domain.command.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.rule.group.GroupNameRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class CreateGroupCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final String groupName;

    public CreateGroupCommand(final String theGroupName) {
        groupName = theGroupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new GroupNameRule(groupName).validate();
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
        CreateGroupCommand rhs = (CreateGroupCommand) obj;
        return new EqualsBuilder()
                .append(this.groupName, rhs.groupName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupName", groupName)
                .toString();
    }
}
