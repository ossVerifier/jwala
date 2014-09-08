package com.siemens.cto.aem.domain.model.state.command;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.state.OperationalState;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.state.CurrentState;

public abstract class SetStateCommand<S, T extends OperationalState> implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final CurrentState<S, T> newState;

    public SetStateCommand(final CurrentState<S, T> theNewState) {
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
        SetStateCommand rhs = (SetStateCommand) obj;
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
