package com.siemens.cto.aem.control.jvm;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface JvmCommandExecutor {

    ExecData controlJvm(final ControlJvmCommand aCommand,
                             final Jvm aJvm) throws CommandFailureException;
}
