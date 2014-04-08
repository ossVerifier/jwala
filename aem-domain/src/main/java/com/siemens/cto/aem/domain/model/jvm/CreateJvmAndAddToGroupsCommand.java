package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;

public class CreateJvmAndAddToGroupsCommand extends CreateJvmCommand implements Serializable, Command {

    private final Set<Identifier<Group>> groups;

    public CreateJvmAndAddToGroupsCommand(final String theName,
                                          final String theHostName,
                                          final Set<Identifier<Group>> theGroups) {
        super(theName,
              theHostName);
        groups = Collections.unmodifiableSet(new HashSet<>(theGroups));
    }

    public Set<AddJvmToGroupCommand> getAssignmentCommandsFor(final Identifier<Jvm> aJvmId) {
        final Set<AddJvmToGroupCommand> addCommands = new HashSet<>();
        for (final Identifier<Group> groupId : groups) {
            addCommands.add(new AddJvmToGroupCommand(groupId,
                                                     aJvmId));
        }

        return addCommands;
    }

    public Set<Identifier<Group>> getGroups() {
        return groups;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        super.validateCommand();
        new GroupIdsRule(groups).validate();
    }
}
