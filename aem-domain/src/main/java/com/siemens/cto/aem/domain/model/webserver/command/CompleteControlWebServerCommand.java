package com.siemens.cto.aem.domain.model.webserver.command;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
        CompleteControlWebServerCommand rhs = (CompleteControlWebServerCommand) obj;
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