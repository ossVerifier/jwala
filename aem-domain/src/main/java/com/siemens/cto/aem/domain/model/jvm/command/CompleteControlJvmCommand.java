package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;

public class CompleteControlJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<JvmControlHistory> controlHistoryId;
    private final ExecData execData;

    public CompleteControlJvmCommand(final Identifier<JvmControlHistory> theControlHistoryId,
                                     final ExecData theExecData) {
        controlHistoryId = theControlHistoryId;
        execData = theExecData;
    }

    public Identifier<JvmControlHistory> getControlHistoryId() {
        return controlHistoryId;
    }

    public ExecData getExecData() {
        return execData;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        //Intentionally empty
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
        CompleteControlJvmCommand rhs = (CompleteControlJvmCommand) obj;
        return new EqualsBuilder()
                .append(this.controlHistoryId, rhs.controlHistoryId)
                .append(this.execData, rhs.execData)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(controlHistoryId)
                .append(execData)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("controlHistoryId", controlHistoryId)
                .append("execData", execData)
                .toString();
    }
}
