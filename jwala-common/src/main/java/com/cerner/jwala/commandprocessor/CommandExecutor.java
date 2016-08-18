package com.cerner.jwala.commandprocessor;

import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;

public interface CommandExecutor {

    CommandOutput execute(final CommandProcessorBuilder builder) throws CommandFailureException;

}
