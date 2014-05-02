package com.siemens.cto.aem.commandprocessor.domain;

import java.io.Serializable;

public class ExecutionData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ExecutionReturnCode returnCode;
    private final String standardOutput;
    private final String standardError;

    public ExecutionData(final ExecutionReturnCode theReturnCode,
                         final String theStandardOutput,
                         final String theStandardError) {
        returnCode = theReturnCode;
        standardOutput = theStandardOutput;
        standardError = theStandardError;
    }

    public ExecutionReturnCode getReturnCode() {
        return returnCode;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public String getStandardError() {
        return standardError;
    }
}
