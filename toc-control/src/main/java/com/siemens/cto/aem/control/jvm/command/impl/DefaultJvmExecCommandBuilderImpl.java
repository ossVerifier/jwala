package com.siemens.cto.aem.control.jvm.command.impl;

import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.control.jvm.command.JvmExecCommandBuilder;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.platform.Platform;
import com.siemens.cto.aem.exec.ExecCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

public class DefaultJvmExecCommandBuilderImpl implements JvmExecCommandBuilder {

    private JpaJvm jvm;
    private JvmControlOperation controlOperation;

    @Override
    public JvmExecCommandBuilder setJvm(final JpaJvm aJvm) {
        jvm = aJvm;
        return this;
    }

    @Override
    public JvmExecCommandBuilder setOperation(final JvmControlOperation anOperation) {
        controlOperation = anOperation;
        return this;
    }

    @Override
    public ExecCommand build() {
        //TODO The platform must come from the Jvm in the future (i.e. once it's ready and available)
        final PlatformCommandProvider provider = PlatformCommandProvider.lookup(Platform.WINDOWS);
        final ServiceCommandBuilder builder = provider.getServiceCommandBuilderFor(controlOperation);
        return builder.buildCommandForService(jvm.getName());
    }
}
