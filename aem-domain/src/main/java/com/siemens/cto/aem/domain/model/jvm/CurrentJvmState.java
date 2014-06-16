package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
        CurrentJvmState rhs = (CurrentJvmState) obj;
        return new EqualsBuilder()
                .append(this.jvmId, rhs.jvmId)
                .append(this.jvmState, rhs.jvmState)
                .append(this.asOf, rhs.asOf)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(jvmId)
                .append(jvmState)
                .append(asOf)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jvmId", jvmId)
                .append("jvmState", jvmState)
                .append("asOf", asOf)
                .toString();
    }
}
