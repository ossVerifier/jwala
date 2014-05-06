package com.siemens.cto.aem.domain.model.exec;

import java.io.Serializable;

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
        return (returnCode.equals(ZERO));
    }

    public boolean wasCompleted() {
        return (returnCode != null);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ExecReturnCode that = (ExecReturnCode) o;

        if (returnCode != null ? !returnCode.equals(that.returnCode) : that.returnCode != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return returnCode != null ? returnCode.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ExecReturnCode{" +
               "returnCode=" + returnCode +
               '}';
    }
}
