package com.cerner.jwala.common.request.state;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.OperationalState;
import com.cerner.jwala.common.request.Request;

import java.io.Serializable;

public abstract class SetStateRequest<S, T extends OperationalState> implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final CurrentState<S, T> newState;

    public SetStateRequest(final CurrentState<S, T> theNewState) {
        newState = theNewState;
    }

    public CurrentState<S, T> getNewState() {
        return newState;
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
        SetStateRequest rhs = (SetStateRequest) obj;
        return new EqualsBuilder()
                .append(this.newState, rhs.newState)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(newState)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("newState", newState)
                .toString();
    }
}
