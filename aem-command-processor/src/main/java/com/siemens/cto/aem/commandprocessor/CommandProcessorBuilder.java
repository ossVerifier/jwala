package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.exception.CommandFailureException;

public interface CommandProcessorBuilder {

    CommandProcessor build() throws CommandFailureException;
}
