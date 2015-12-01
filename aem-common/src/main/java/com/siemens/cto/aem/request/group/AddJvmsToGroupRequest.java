package com.siemens.cto.aem.request.group;

import com.siemens.cto.aem.request.Request;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.group.GroupIdRule;
import com.siemens.cto.aem.rule.jvm.JvmIdsRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AddJvmsToGroupRequest implements Request {

    private final Identifier<Group> groupId;
    private final Set<Identifier<Jvm>> jvmIds;

    public AddJvmsToGroupRequest(final Identifier<Group> theGroupId,
                                 final Set<Identifier<Jvm>> theJvmIds) {
        groupId = theGroupId;
        jvmIds = Collections.unmodifiableSet(new HashSet<>(theJvmIds));
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public Set<AddJvmToGroupRequest> toCommands() {
        final Set<AddJvmToGroupRequest> addCommands = new HashSet<>();
        for (final Identifier<Jvm> jvmId : jvmIds) {
            addCommands.add(new AddJvmToGroupRequest(groupId,
                                                     jvmId));
        }

        return addCommands;
    }

    @Override
    public void validate() throws BadRequestException {
        new MultipleRules(new GroupIdRule(groupId),
                                new JvmIdsRule(jvmIds)).validate();
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
        AddJvmsToGroupRequest rhs = (AddJvmsToGroupRequest) obj;
        return new EqualsBuilder()
                .append(this.groupId, rhs.groupId)
                .append(this.jvmIds, rhs.jvmIds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupId)
                .append(jvmIds)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("jvmIds", jvmIds)
                .toString();
    }
}
