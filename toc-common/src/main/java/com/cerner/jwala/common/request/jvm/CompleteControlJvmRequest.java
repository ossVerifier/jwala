package com.cerner.jwala.common.request.jvm;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.Request;

import java.io.Serializable;

public class CompleteControlJvmRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final CommandOutput execData;

    public CompleteControlJvmRequest(final CommandOutput theExecData) {
        execData = theExecData;
    }

    public CommandOutput getExecData() {
        return execData;
    }

    @Override
    public void validate() {
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
        CompleteControlJvmRequest rhs = (CompleteControlJvmRequest) obj;
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
