package com.siemens.cto.aem.control.jvm;

import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.command.jvm.ControlJvmCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface JvmCommandExecutor {

    CommandOutput controlJvm(final ControlJvmCommand aCommand,
                             final Jvm aJvm) throws CommandFailureException;
}
