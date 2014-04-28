package com.siemens.cto.aem.commandprocessor.impl.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;

public class LocalRuntimeCommandProcessorImpl implements CommandProcessor {

    private final Process process;
    private boolean wasClosed;
    private boolean wasTerminatedAbnormally;

    public LocalRuntimeCommandProcessorImpl(final ExecCommand theCommand) throws IOException {

        this(theCommand,
             new ProcessBuilder());
    }

    public LocalRuntimeCommandProcessorImpl(final ExecCommand theCommand,
                                            final ProcessBuilder theProcessBuilder) throws IOException {

        process = theProcessBuilder.command(theCommand.getCommandFragments()).start();
        wasClosed = false;
        wasTerminatedAbnormally = false;
    }

    @Override
    public InputStream getCommandOutput() {
        return process.getInputStream();
    }

    @Override
    public InputStream getErrorOutput() {
        return process.getErrorStream();
    }

    @Override
    public OutputStream getCommandInput() {
        return process.getOutputStream();
    }

    @Override
    public void close() throws Exception {
        try {
            wasClosed = true;
            process.exitValue();
        } catch (final IllegalThreadStateException itse) {
            process.destroy();
            wasTerminatedAbnormally = true;
        }
    }

    public boolean wasClosed() {
        return wasClosed;
    }

    public boolean wasTerminatedAbnormally() {
        return wasTerminatedAbnormally;
    }
}
