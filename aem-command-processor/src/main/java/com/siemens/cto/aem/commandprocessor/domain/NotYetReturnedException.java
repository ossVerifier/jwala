package com.siemens.cto.aem.commandprocessor.domain;

public class NotYetReturnedException extends Exception {

    private final ExecCommand command;

    public NotYetReturnedException(final ExecCommand theCommand) {
        this(theCommand,
             null);
    }

    public NotYetReturnedException(final ExecCommand theCommand,
                                   final Throwable theCause) {
        super("The command has not yet completed when attempting to retrieve the return code",
              theCause);
        command = theCommand;
    }

    public ExecCommand getCommand() {
        return command;
    }
}
