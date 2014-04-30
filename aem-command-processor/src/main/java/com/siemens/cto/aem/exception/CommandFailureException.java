package com.siemens.cto.aem.exception;

import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;

public class CommandFailureException extends Exception {

    private final ExecCommand command;

    public CommandFailureException(final ExecCommand aCommand,
                                   final Throwable aCause) {
        super(aCause);
        command = aCommand;
    }

    public ExecCommand getCommand() {
        return command;
    }
}
