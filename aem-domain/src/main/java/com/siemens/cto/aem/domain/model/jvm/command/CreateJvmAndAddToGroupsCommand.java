package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;

public class CreateJvmAndAddToGroupsCommand implements Serializable, Command {

    private final CreateJvmCommand createCommand;
    private final Set<Identifier<Group>> groups;

    public CreateJvmAndAddToGroupsCommand(final String theName,
                                          final String theHostName,
                                          final Set<Identifier<Group>> theGroups,
                                          final Integer theHttpPort,
                                          final Integer theHttpsPort,
                                          final Integer theRedirectPort,
                                          final Integer theShutdownPort,
                                          final Integer theAjpPort) {

        createCommand = new CreateJvmCommand(theName,
                                             theHostName,
                                             theHttpPort,
                                             theHttpsPort,
                                             theRedirectPort,
                                             theShutdownPort,
                                             theAjpPort);
        groups = Collections.unmodifiableSet(new HashSet<>(theGroups));
    }

    public CreateJvmCommand getCreateCommand() {
        return createCommand;
    }

    public Set<AddJvmToGroupCommand> toAddCommandsFor(final Identifier<Jvm> aJvmId) {
        return new AddJvmToGroupCommandSetBuilder(aJvmId,
                                                  groups).build();
    }

    public Set<Identifier<Group>> getGroups() {
        return groups;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        createCommand.validateCommand();
        new GroupIdsRule(groups).validate();
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
        CreateJvmAndAddToGroupsCommand rhs = (CreateJvmAndAddToGroupsCommand) obj;
        return new EqualsBuilder()
                .append(this.createCommand, rhs.createCommand)
                .append(this.groups, rhs.groups)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(createCommand)
                .append(groups)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("createCommand", createCommand)
                .append("groups", groups)
                .toString();
    }
}
