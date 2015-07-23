package com.siemens.cto.aem.domain.model.jvm;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class JvmControlHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<JvmControlHistory> id;
    private final Identifier<Jvm> jvmId;
    private final JvmControlOperation controlOperation;
    private final AuditEvent whenRequested;
    private final ExecData execData;

    public JvmControlHistory(final Identifier<JvmControlHistory> theId,
                             final Identifier<Jvm> theJvmId,
                             final JvmControlOperation theControlOperation,
                             final AuditEvent theWhenRequested,
                             final ExecData theExecData) {
        id = theId;
        jvmId = theJvmId;
        controlOperation = theControlOperation;
        whenRequested = theWhenRequested;
        execData = theExecData;
    }

    public Identifier<JvmControlHistory> getId() {
        return id;
    }

    public Identifier<Jvm> getJvmId() {
        return jvmId;
    }

    public JvmControlOperation getControlOperation() {
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
        JvmControlHistory rhs = (JvmControlHistory) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.jvmId, rhs.jvmId)
                .append(this.controlOperation, rhs.controlOperation)
                .append(this.whenRequested, rhs.whenRequested)
                .append(this.execData, rhs.execData)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(jvmId)
                .append(controlOperation)
                .append(whenRequested)
                .append(execData)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("jvmId", jvmId)
                .append("controlOperation", controlOperation)
                .append("whenRequested", whenRequested)
                .append("execData", execData)
                .toString();
    }
}
