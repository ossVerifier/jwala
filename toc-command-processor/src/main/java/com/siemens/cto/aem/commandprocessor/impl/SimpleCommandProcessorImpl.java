package com.siemens.cto.aem.commandprocessor.impl;

import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.SimpleCommandProcessor;
import com.siemens.cto.aem.io.FullInputStreamReader;

import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class SimpleCommandProcessorImpl implements SimpleCommandProcessor {

    private final String commandOutput;
    private final String errorOutput;

    public SimpleCommandProcessorImpl(final CommandProcessor theCommandProcessor) throws IOException {
        commandOutput = theCommandProcessor.getCommandOutputStr();
        errorOutput = theCommandProcessor.getErrorOutputStr();
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
