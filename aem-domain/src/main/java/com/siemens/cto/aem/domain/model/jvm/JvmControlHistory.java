package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JvmControlHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<JvmControlHistory> id;
    private final Identifier<Jvm> jvmId;
    private final JvmControlOperation controlOperation;
    private final AuditEvent whenRequested;

    public JvmControlHistory(final Identifier<JvmControlHistory> theId,
                             final Identifier<Jvm> theJvmId,
                             final JvmControlOperation theControlOperation,
                             final AuditEvent theWhenRequested) {
        id = theId;
        jvmId = theJvmId;
        controlOperation = theControlOperation;
        whenRequested = theWhenRequested;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JvmControlHistory that = (JvmControlHistory) o;

        if (controlOperation != that.controlOperation) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (jvmId != null ? !jvmId.equals(that.jvmId) : that.jvmId != null) {
            return false;
        }
        if (whenRequested != null ? !whenRequested.equals(that.whenRequested) : that.whenRequested != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (jvmId != null ? jvmId.hashCode() : 0);
        result = 31 * result + (controlOperation != null ? controlOperation.hashCode() : 0);
        result = 31 * result + (whenRequested != null ? whenRequested.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JvmControlHistory{" +
               "id=" + id +
               ", jvmId=" + jvmId +
               ", controlOperation=" + controlOperation +
               ", whenRequested=" + whenRequested +
               '}';
    }
}
