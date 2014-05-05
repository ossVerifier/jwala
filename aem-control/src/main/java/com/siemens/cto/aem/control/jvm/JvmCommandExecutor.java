package com.siemens.cto.aem.control.jvm;

import com.siemens.cto.aem.commandprocessor.domain.ExecutionData;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface JvmCommandExecutor {

    ExecutionData controlJvm(final ControlJvmCommand aCommand,
                             final Jvm aJvm) throws CommandFailureException;
}
