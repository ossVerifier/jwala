package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface CommandExecutor {

    CommandOutput execute(final CommandProcessorBuilder builder) throws CommandFailureException;

}
