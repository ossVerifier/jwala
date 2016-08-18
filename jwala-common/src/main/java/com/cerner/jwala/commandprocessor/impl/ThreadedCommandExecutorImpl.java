package com.cerner.jwala.commandprocessor.impl;

import com.cerner.jwala.commandprocessor.CommandExecutor;
import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.commandprocessor.CommandProcessorBuilder;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;

public class ThreadedCommandExecutorImpl implements CommandExecutor {

    @Override
    public CommandOutput execute(final CommandProcessorBuilder commandProcessorBuilder) throws CommandFailureException {
        final CommandProcessor processor = commandProcessorBuilder.build();
        processor.processCommand();
        return new CommandOutput(processor.getExecutionReturnCode(), processor.getCommandOutputStr(), processor.getErrorOutputStr());
    }

}
