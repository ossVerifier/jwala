package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdRule;

public class ControlJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<Jvm> jvmId;
    private final JvmControlOperation controlOperation;

    public ControlJvmCommand(final Identifier<Jvm> theId,
                             final JvmControlOperation theControlOperation) {
        jvmId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<Jvm> getJvmId() {
        return jvmId;
    }

    public JvmControlOperation getControlOperation() {
        return controlOperation;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new JvmIdRule(jvmId).validate();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ControlJvmCommand that = (ControlJvmCommand) o;

        if (controlOperation != that.controlOperation) {
            return false;
        }
        if (jvmId != null ? !jvmId.equals(that.jvmId) : that.jvmId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = jvmId != null ? jvmId.hashCode() : 0;
        result = 31 * result + (controlOperation != null ? controlOperation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ControlJvmCommand{" +
               "jvmId=" + jvmId +
               ", controlOperation=" + controlOperation +
               '}';
    }
}
