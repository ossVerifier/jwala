package com.siemens.cto.aem.commandprocessor.impl;

import java.io.IOException;
import java.io.InputStream;

import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.SimpleCommandProcessor;
import com.siemens.cto.aem.io.FullInputStreamReader;

public class SimpleCommandProcessorImpl implements SimpleCommandProcessor {

    private final CommandProcessor commandProcessor;
    private final String commandOutput;
    private final String errorOutput;

    public SimpleCommandProcessorImpl(final String theCommand) throws IOException {
        //TODO use the command when constructing the command processor
        commandProcessor = null;
        commandOutput = readAllOutput(commandProcessor.getCommandOutput());
        errorOutput = readAllOutput(commandProcessor.getErrorOutput());
    }

    @Override
    public String getCommandOutput() {
        return commandOutput;
    }

    @Override
    public String getErrorOutput() {
        return errorOutput;
    }

    protected String readAllOutput(final InputStream anInputStream) throws IOException {
        final FullInputStreamReader reader = new FullInputStreamReader(anInputStream);
        return reader.readString();
    }
}
