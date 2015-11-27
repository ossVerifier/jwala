package com.siemens.cto.aem.control.jvm.command;

import com.siemens.cto.aem.domain.command.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;

public interface JvmExecCommandBuilder {

    JvmExecCommandBuilder setJvm(final Jvm aJvm);

    JvmExecCommandBuilder setOperation(final JvmControlOperation anOperation);

    ExecCommand build();
}
