package com.siemens.cto.aem.domain.model.state;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class CurrentState<S, T extends ExternalizableState> {

    private final Identifier<S> id;
    private final T state;
    private final DateTime asOf;

    public CurrentState(final Identifier<S> theId,
                        final T theState,
                        final DateTime theAsOf) {
        id = theId;
        state = theState;
        asOf = theAsOf;
    }

    public Identifier<S> getId() {
        return id;
    }

    public T getState() {
        return state;
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
        CurrentState<S, T> rhs = (CurrentState<S, T>) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.state, rhs.state)
                .append(this.asOf, rhs.asOf)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(state)
                .append(asOf)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("state", state)
                .append("asOf", asOf)
                .toString();
    }
}
