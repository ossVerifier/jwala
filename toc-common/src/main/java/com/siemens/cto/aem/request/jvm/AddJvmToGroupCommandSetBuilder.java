package com.siemens.cto.aem.request.jvm;

import com.siemens.cto.aem.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

import java.util.HashSet;
import java.util.Set;

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

    public Set<AddJvmToGroupRequest> build() {
        final Set<AddJvmToGroupRequest> commands = new HashSet<>();
        for (final Identifier<Group> groupId : groupIds) {
            commands.add(new AddJvmToGroupRequest(groupId,
                                                  jvmId));
        }
        return commands;
    }
}
