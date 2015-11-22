package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.domain.model.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface CommandExecutor {

    CommandOutput execute(final CommandProcessorBuilder aBuilder) throws CommandFailureException;
}
