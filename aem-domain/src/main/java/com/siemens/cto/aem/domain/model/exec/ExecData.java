package com.siemens.cto.aem.domain.model.exec;

import java.io.Serializable;

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
}
