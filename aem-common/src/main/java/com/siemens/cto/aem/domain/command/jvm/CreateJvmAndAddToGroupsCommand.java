package com.siemens.cto.aem.domain.command.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.command.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.rule.group.GroupIdsRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CreateJvmAndAddToGroupsCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;
    private final CreateJvmCommand createCommand;
    private final Set<Identifier<Group>> groups;

    public CreateJvmAndAddToGroupsCommand(final String theName,
                                          final String theHostName,
                                          final Set<Identifier<Group>> theGroups,
                                          final Integer theHttpPort,
                                          final Integer theHttpsPort,
                                          final Integer theRedirectPort,
                                          final Integer theShutdownPort,
                                          final Integer theAjpPort,
                                          final Path theStatusPath,
                                          final String theSystemProperties) {

        createCommand = new CreateJvmCommand(theName,
                                             theHostName,
                                             theHttpPort,
                                             theHttpsPort,
                                             theRedirectPort,
                                             theShutdownPort,
                                             theAjpPort,
                                             theStatusPath,
                                             theSystemProperties);
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
