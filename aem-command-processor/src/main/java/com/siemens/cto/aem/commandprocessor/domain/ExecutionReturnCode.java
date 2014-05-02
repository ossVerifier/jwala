package com.siemens.cto.aem.commandprocessor.domain;

import java.io.Serializable;

public class ExecutionReturnCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Integer ZERO = Integer.valueOf(0);

    private final Integer returnCode;

    public ExecutionReturnCode(final Integer theReturnCode) {
        returnCode = theReturnCode;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public boolean wasSuccessful() {
        return (returnCode.equals(ZERO));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ExecutionReturnCode that = (ExecutionReturnCode) o;

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
        return "ExecutionReturnCode{" +
               "returnCode=" + returnCode +
               '}';
    }
}
