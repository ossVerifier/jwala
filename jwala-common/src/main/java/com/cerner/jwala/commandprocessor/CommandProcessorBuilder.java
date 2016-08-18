package com.cerner.jwala.commandprocessor;

import com.cerner.jwala.exception.CommandFailureException;

public interface CommandProcessorBuilder {

    CommandProcessor build() throws CommandFailureException;
}
