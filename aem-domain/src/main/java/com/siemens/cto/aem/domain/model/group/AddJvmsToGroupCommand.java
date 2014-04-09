package com.siemens.cto.aem.domain.model.group;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdsRule;

public class AddJvmsToGroupCommand implements Command {

    private final Identifier<Group> groupId;
    private final Set<Identifier<Jvm>> jvmIds;

    public AddJvmsToGroupCommand(final Identifier<Group> theGroupId,
                                 final Set<Identifier<Jvm>> theJvmIds) {
        groupId = theGroupId;
        jvmIds = Collections.unmodifiableSet(new HashSet<Identifier<Jvm>>(theJvmIds));
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public Set<AddJvmToGroupCommand> toCommands() {
        final Set<AddJvmToGroupCommand> addCommands = new HashSet<>();
        for (final Identifier<Jvm> jvmId : jvmIds) {
            addCommands.add(new AddJvmToGroupCommand(groupId,
                                                     jvmId));
        }

        return addCommands;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRuleCommand(new GroupIdRule(groupId),
                                new JvmIdsRule(jvmIds)).validateCommand();
    }
}
