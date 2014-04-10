package com.siemens.cto.aem.domain.model.jvm;

import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class AddJvmToGroupCommandSetBuilder {

    private Identifier<Jvm> jvmId;
    private Set<Identifier<Group>> groupIds;

    public AddJvmToGroupCommandSetBuilder() {
    }

    public AddJvmToGroupCommandSetBuilder(final Identifier<Jvm> aJvmId,
                                          final Set<Identifier<Group>> someGroupIds) {
        jvmId = aJvmId;
        groupIds = someGroupIds;
    }

    public AddJvmToGroupCommandSetBuilder setJvmId(final Identifier<Jvm> aJvmId) {
        jvmId = aJvmId;
        return this;
    }

    public AddJvmToGroupCommandSetBuilder setGroupIds(final Set<Identifier<Group>> someGroupIds) {
        groupIds = someGroupIds;
        return this;
    }

    public Set<AddJvmToGroupCommand> build() {
        final Set<AddJvmToGroupCommand> commands = new HashSet<>();
        for (final Identifier<Group> groupId : groupIds) {
            commands.add(new AddJvmToGroupCommand(groupId,
                                                  jvmId));
        }
        return commands;
    }
}
