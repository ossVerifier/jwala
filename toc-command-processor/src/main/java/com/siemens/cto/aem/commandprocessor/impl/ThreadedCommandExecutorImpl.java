package com.siemens.cto.aem.commandprocessor.impl;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.exception.NotYetReturnedException;

public class ThreadedCommandExecutorImpl implements CommandExecutor {

    @Override
    public CommandOutput execute(final CommandProcessorBuilder commandProcessorBuilder) throws CommandFailureException {
        final CommandProcessor processor = commandProcessorBuilder.build();
        processor.processCommand();

        try {
            return new CommandOutput(processor.getExecutionReturnCode(), processor.getCommandOutputStr(), processor.getErrorOutputStr());
        } catch (final NotYetReturnedException nyre) {
            throw new CommandFailureException(nyre.getCommand(), nyre);
        }
    }

}
