package com.siemens.cto.aem.domain.model.exec;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ExecReturnCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Integer ZERO = 0;

    private final Integer returnCode;

    public ExecReturnCode(final Integer theReturnCode) {
        returnCode = theReturnCode;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public Boolean getWasSuccessful() {
        return wasSuccessful();
    }

    public Boolean getWasCompleted() {
        return wasCompleted();
    }

    public boolean wasSuccessful() {
        System.out.println("WasSuccessful("+ (returnCode.equals(ZERO)) + ")");
        return (returnCode.equals(ZERO));
    }

    public boolean wasCompleted() {
        return (returnCode != null);
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
        ExecReturnCode rhs = (ExecReturnCode) obj;
        return new EqualsBuilder()
                .append(this.returnCode, rhs.returnCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(returnCode)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("returnCode", returnCode)
                .toString();
    }
}
