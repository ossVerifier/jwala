package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmStateRule;

public class SetJvmStateCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final CurrentJvmState newJvmState;

    public SetJvmStateCommand(final CurrentJvmState theNewState) {
        newJvmState = theNewState;
    }

    public CurrentJvmState getNewJvmState() {
        return newJvmState;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        //TODO Not sure how I feel about rules anymore (they seem to be simply around presence of data...for now)
        new MultipleRules(new JvmIdRule(newJvmState.getJvmId()),
                          new JvmStateRule(newJvmState.getJvmState())).validate();
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
        SetJvmStateCommand rhs = (SetJvmStateCommand) obj;
        return new EqualsBuilder()
                .append(this.newJvmState, rhs.newJvmState)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(newJvmState)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("newJvmState", newJvmState)
                .toString();
    }
}
