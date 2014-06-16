package com.siemens.cto.aem.domain.model.exec;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ExecData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ExecReturnCode returnCode;
    private final String standardOutput;
    private final String standardError;

    public ExecData(final ExecReturnCode theReturnCode,
                    final String theStandardOutput,
                    final String theStandardError) {
        returnCode = theReturnCode;
        standardOutput = theStandardOutput;
        standardError = theStandardError;
    }

    public ExecReturnCode getReturnCode() {
        return returnCode;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public String getStandardError() {
        return standardError;
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
        ExecData rhs = (ExecData) obj;
        return new EqualsBuilder()
                .append(this.returnCode, rhs.returnCode)
                .append(this.standardOutput, rhs.standardOutput)
                .append(this.standardError, rhs.standardError)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(returnCode)
                .append(standardOutput)
                .append(standardError)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("returnCode", returnCode)
                .append("standardOutput", standardOutput)
                .append("standardError", standardError)
                .toString();
    }
}
