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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ExecData execData = (ExecData) o;

        if (returnCode != null ? !returnCode.equals(execData.returnCode) : execData.returnCode != null) {
            return false;
        }
        if (standardError != null ? !standardError.equals(execData.standardError) : execData.standardError != null) {
            return false;
        }
        if (standardOutput != null ? !standardOutput.equals(execData.standardOutput) : execData.standardOutput != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = returnCode != null ? returnCode.hashCode() : 0;
        result = 31 * result + (standardOutput != null ? standardOutput.hashCode() : 0);
        result = 31 * result + (standardError != null ? standardError.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExecData{" +
               "returnCode=" + returnCode +
               ", standardOutput='" + standardOutput + '\'' +
               ", standardError='" + standardError + '\'' +
               '}';
    }
}
