package com.siemens.cto.aem.domain.model.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class AbstractJvmGroupCommand implements Command {

    private final Identifier<Group> groupId;
    private final Identifier<Jvm> jvmId;

    public AbstractJvmGroupCommand(final Identifier<Group> theGroupId,
                                   final Identifier<Jvm> theJvmId) {
        groupId = theGroupId;
        jvmId = theJvmId;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public Identifier<Jvm> getJvmId() {
        return jvmId;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new GroupIdRule(groupId),
                                new JvmIdRule(jvmId)).validate();
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
        AbstractJvmGroupCommand rhs = (AbstractJvmGroupCommand) obj;
        return new EqualsBuilder()
                .append(this.groupId, rhs.groupId)
                .append(this.jvmId, rhs.jvmId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupId)
                .append(jvmId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("jvmId", jvmId)
                .toString();
    }
}
