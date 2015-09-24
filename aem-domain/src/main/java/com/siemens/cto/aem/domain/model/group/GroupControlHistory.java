package com.siemens.cto.aem.domain.model.group;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class GroupControlHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<GroupControlHistory> id;
    private final Identifier<Group> groupId;
    private final GroupControlOperation controlOperation;
    private final AuditEvent whenRequested;

    public GroupControlHistory(final Identifier<GroupControlHistory> theId,
                               final Identifier<Group> theGroupId,
                               final GroupControlOperation theControlOperation,
                               final AuditEvent theWhenRequested) {
        id = theId;
        groupId = theGroupId;
        controlOperation = theControlOperation;
        whenRequested = theWhenRequested;
    }

    public Identifier<GroupControlHistory> getId() {
        return id;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public GroupControlOperation getControlOperation() {
        return controlOperation;
    }

    public AuditEvent getWhenRequested() {
        return whenRequested;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupControlHistory that = (GroupControlHistory) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(groupId, that.groupId)
                .append(controlOperation, that.controlOperation)
                .append(whenRequested, that.whenRequested)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(groupId)
                .append(controlOperation)
                .append(whenRequested)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "GroupControlHistory [id=" + id + ", groupId=" + groupId + ", controlOperation=" + controlOperation
                + ", whenRequested=" + whenRequested + "]";
    }
}
