package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface CommandExecutor {

    ExecData execute(final CommandProcessorBuilder aBuilder) throws CommandFailureException;
}
