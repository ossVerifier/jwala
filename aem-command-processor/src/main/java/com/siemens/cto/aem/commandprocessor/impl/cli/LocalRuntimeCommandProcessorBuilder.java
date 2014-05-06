package com.siemens.cto.aem.commandprocessor.impl.cli;

import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public class LocalRuntimeCommandProcessorBuilder implements CommandProcessorBuilder {

    private ExecCommand command;

    public LocalRuntimeCommandProcessorBuilder() {
    }

    public LocalRuntimeCommandProcessorBuilder(final ExecCommand aCommand) {
        command = aCommand;
    }

    public LocalRuntimeCommandProcessorBuilder setCommand(final ExecCommand aCommand) {
        command = aCommand;
        return this;
    }

    @Override
    public CommandProcessor build() throws CommandFailureException {
        return new LocalRuntimeCommandProcessorImpl(command);
    }
}
