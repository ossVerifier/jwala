package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.commandprocessor.domain.ExecutionData;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface CommandExecutor {

    ExecutionData execute(final CommandProcessorBuilder aBuilder) throws CommandFailureException;
}
