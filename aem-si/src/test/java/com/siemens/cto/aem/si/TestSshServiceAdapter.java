package com.siemens.cto.aem.si;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class TestSshServiceAdapter {

    private ExecData execData;
    private Jvm jvmInError;

    public void completeRequest(final ExecData someExecData) {
        execData = someExecData;
    }

    public void completeErrorRequest(final Jvm aJvmInError) {
        jvmInError = aJvmInError;
    }

    public ExecData getExecData() {
        return execData;
    }

    public Jvm getJvmInError() {
        return jvmInError;
    }
}
