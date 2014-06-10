package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;

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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SetJvmStateCommand that = (SetJvmStateCommand) o;

        if (newJvmState != null ? !newJvmState.equals(that.newJvmState) : that.newJvmState != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return newJvmState != null ? newJvmState.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SetJvmStateCommand{" +
               "newJvmState=" + newJvmState +
               '}';
    }
}
