package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;

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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CompleteControlJvmCommand that = (CompleteControlJvmCommand) o;

        if (controlHistoryId != null ? !controlHistoryId.equals(that.controlHistoryId) : that.controlHistoryId != null) {
            return false;
        }
        if (execData != null ? !execData.equals(that.execData) : that.execData != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = controlHistoryId != null ? controlHistoryId.hashCode() : 0;
        result = 31 * result + (execData != null ? execData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompleteControlJvmCommand{" +
               "controlHistoryId=" + controlHistoryId +
               ", execData=" + execData +
               '}';
    }
}
