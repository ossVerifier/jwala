package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class CurrentJvmState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Jvm> jvmId;
    private final JvmState jvmState;
    private final DateTime asOf;

    public CurrentJvmState(final Identifier<Jvm> theJvmId,
                           final JvmState theJvmState,
                           final DateTime theAsOf) {
        jvmId = theJvmId;
        jvmState = theJvmState;
        asOf = theAsOf;
    }

    public Identifier<Jvm> getJvmId() {
        return jvmId;
    }

    public JvmState getJvmState() {
        return jvmState;
    }

    public DateTime getAsOf() {
        return asOf;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CurrentJvmState that = (CurrentJvmState) o;

        if (asOf != null ? !asOf.equals(that.asOf) : that.asOf != null) {
            return false;
        }
        if (jvmId != null ? !jvmId.equals(that.jvmId) : that.jvmId != null) {
            return false;
        }
        if (jvmState != that.jvmState) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = jvmId != null ? jvmId.hashCode() : 0;
        result = 31 * result + (jvmState != null ? jvmState.hashCode() : 0);
        result = 31 * result + (asOf != null ? asOf.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CurrentJvmState{" +
               "jvmId=" + jvmId +
               ", jvmState=" + jvmState +
               ", asOf=" + asOf +
               '}';
    }
}
