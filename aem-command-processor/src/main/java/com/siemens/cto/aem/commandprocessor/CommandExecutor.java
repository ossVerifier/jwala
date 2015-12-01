package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface CommandExecutor {

    CommandOutput execute(final CommandProcessorBuilder aBuilder) throws CommandFailureException;
}
