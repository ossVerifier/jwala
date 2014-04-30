package com.siemens.cto.aem.commandprocessor.impl.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;
import com.siemens.cto.aem.commandprocessor.domain.ExecutionReturnCode;
import com.siemens.cto.aem.commandprocessor.domain.NotYetReturnedException;
import com.siemens.cto.aem.exception.CommandFailureException;

public class LocalRuntimeCommandProcessorImpl implements CommandProcessor {

    private final Process process;
    private boolean wasClosed;
    private boolean wasTerminatedAbnormally;
    private final ExecCommand command;

    public LocalRuntimeCommandProcessorImpl(final ExecCommand theCommand) throws CommandFailureException {

        this(theCommand,
             new ProcessBuilder());
    }

    public LocalRuntimeCommandProcessorImpl(final ExecCommand theCommand,
                                            final ProcessBuilder theProcessBuilder) throws CommandFailureException {

        try {
            command = theCommand;
            process = theProcessBuilder.command(theCommand.getCommandFragments()).start();
            wasClosed = false;
            wasTerminatedAbnormally = false;
        } catch (final IOException ioe) {
            throw new CommandFailureException(theCommand,
                                              ioe);
        }
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
    public void close() {
        if (!wasClosed) {
            try {
                wasClosed = true;
                process.exitValue();
            } catch (final IllegalThreadStateException itse) {
                process.destroy();
                wasTerminatedAbnormally = true;
            }
        }
    }

    @Override
    public ExecutionReturnCode getExecutionReturnCode() throws NotYetReturnedException {
        try {
            final Integer returnCode = process.exitValue();
            return new ExecutionReturnCode(returnCode);
        } catch (final IllegalThreadStateException itse) {
            throw new NotYetReturnedException(command,
                                              itse);
        }
    }

    public boolean wasClosed() {
        return wasClosed;
    }

    public boolean wasTerminatedAbnormally() {
        return wasTerminatedAbnormally;
    }
}
