package com.siemens.cto.aem.domain.model.webserver.command;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;

import java.io.Serializable;

public class CompleteControlWebServerCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServerControlHistory> controlHistoryId;
    private final ExecData execData;

    public CompleteControlWebServerCommand(final Identifier<WebServerControlHistory> theControlHistoryId,
                                           final ExecData theExecData) {
        controlHistoryId = theControlHistoryId;
        execData = theExecData;
    }

    public Identifier<WebServerControlHistory> getControlHistoryId() {
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

        final CompleteControlWebServerCommand that = (CompleteControlWebServerCommand) o;

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
        return "CompleteControlWebServerCommand{" +
               "controlHistoryId=" + controlHistoryId +
               ", execData=" + execData +
               '}';
    }
}