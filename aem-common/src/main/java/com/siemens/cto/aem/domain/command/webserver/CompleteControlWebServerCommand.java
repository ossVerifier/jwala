package com.siemens.cto.aem.domain.command.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class CompleteControlWebServerCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final CommandOutput execData;

    public CompleteControlWebServerCommand(final CommandOutput theExecData) {
        execData = theExecData;
    }

    public CommandOutput getExecData() {
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
                .append(this.execData, rhs.execData)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(execData)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("execData", execData)
                .toString();
    }
}