package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class GroupControlHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<GroupControlHistory> id;
    private final Identifier<Group> groupId;
    private final GroupControlOperation controlOperation;
    private final AuditEvent whenRequested;
    private final ExecData execData;  // don't think I need this for group...

    public GroupControlHistory(final Identifier<GroupControlHistory> theId,
                             final Identifier<Group> theGroupId,
                             final GroupControlOperation theControlOperation,
                             final AuditEvent theWhenRequested,
                             final ExecData theExecData) {
        id = theId;
        groupId = theGroupId;
        controlOperation = theControlOperation;
        whenRequested = theWhenRequested;
        execData = theExecData;
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

    public ExecData getExecData() {
        return execData;
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
        GroupControlHistory rhs = (GroupControlHistory) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.groupId, rhs.groupId)
                .append(this.controlOperation, rhs.controlOperation)
                .append(this.whenRequested, rhs.whenRequested)
                .append(this.execData, rhs.execData)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(groupId)
                .append(controlOperation)
                .append(whenRequested)
                .append(execData)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("jvmId", groupId)
                .append("controlOperation", controlOperation)
                .append("whenRequested", whenRequested)
                .append("execData", execData)
                .toString();
    }
}
