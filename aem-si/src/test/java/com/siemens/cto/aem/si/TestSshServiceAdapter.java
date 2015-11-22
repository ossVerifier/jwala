package com.siemens.cto.aem.si;

import com.siemens.cto.aem.domain.model.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class TestSshServiceAdapter {

    private CommandOutput execData;
    private Jvm jvmInError;

    public void completeRequest(final CommandOutput someExecData) {
        execData = someExecData;
    }

    public void completeErrorRequest(final Jvm aJvmInError) {
        jvmInError = aJvmInError;
    }

    public CommandOutput getExecData() {
        return execData;
    }

    public Jvm getJvmInError() {
        return jvmInError;
    }
}
