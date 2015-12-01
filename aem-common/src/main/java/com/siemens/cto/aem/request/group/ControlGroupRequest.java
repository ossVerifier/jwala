package com.siemens.cto.aem.request.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.request.MultipleRuleRequest;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.rule.group.GroupControlOperationRule;
import com.siemens.cto.aem.rule.group.GroupIdRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class ControlGroupRequest implements Serializable, GroupRequest {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> groupId;
    private final GroupControlOperation controlOperation;

    public ControlGroupRequest(final Identifier<Group> theId,
                               final GroupControlOperation theControlOperation) {
        groupId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public GroupControlOperation getControlOperation() {
        return controlOperation;
    }

    
    @Override
    public void validate() {
        // can only partially validate without state
        new GroupIdRule(groupId).validate();
    }

    public void validateCommand(
            final boolean canStart,
            final boolean canStop) throws BadRequestException {
        new MultipleRuleRequest(
                new GroupIdRule(groupId),
                new GroupControlOperationRule(controlOperation, canStart, canStop)
        ).validate();
    }

    @Override
    public String getExternalOperationName() {
        return controlOperation.getExternalValue();
    }

    @Override
    public String getType() {
        return "Group";
    }
    
    @Override
    public Long getId() {
        return groupId.getId();
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
        ControlGroupRequest rhs = (ControlGroupRequest) obj;
        return new EqualsBuilder()
                .append(this.groupId, rhs.groupId)
                .append(this.controlOperation, rhs.controlOperation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupId)
                .append(controlOperation)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupId", groupId)
                .append("controlOperation", controlOperation)
                .toString();
    }

}
