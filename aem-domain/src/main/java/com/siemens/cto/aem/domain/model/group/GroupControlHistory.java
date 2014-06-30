package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controlOperation == null) ? 0 : controlOperation.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((whenRequested == null) ? 0 : whenRequested.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupControlHistory other = (GroupControlHistory) obj;
        if (controlOperation != other.controlOperation)
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (whenRequested == null) {
            if (other.whenRequested != null)
                return false;
        } else if (!whenRequested.equals(other.whenRequested))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "GroupControlHistory [id=" + id + ", groupId=" + groupId + ", controlOperation=" + controlOperation
                + ", whenRequested=" + whenRequested + "]";
    }
}
