package com.siemens.cto.aem.request.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.request.Request;
import com.siemens.cto.aem.exec.CommandOutput;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class CompleteControlWebServerRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final CommandOutput execData;

    public CompleteControlWebServerRequest(final CommandOutput theExecData) {
        execData = theExecData;
    }

    public CommandOutput getExecData() {
        return execData;
    }

    @Override
    public void validate() throws BadRequestException {
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
        CompleteControlWebServerRequest rhs = (CompleteControlWebServerRequest) obj;
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