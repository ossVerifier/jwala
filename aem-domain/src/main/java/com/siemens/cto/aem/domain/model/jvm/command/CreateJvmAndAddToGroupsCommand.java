package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
                                          final Set<Identifier<Group>> theGroups) {

        createCommand = new CreateJvmCommand(theName,
                                             theHostName);
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
}
