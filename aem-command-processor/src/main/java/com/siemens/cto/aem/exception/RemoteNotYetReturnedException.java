package com.siemens.cto.aem.exception;

import com.siemens.cto.aem.domain.command.exec.RemoteExecCommand;

public class RemoteNotYetReturnedException extends NotYetReturnedException {

    private final RemoteExecCommand remoteCommand;

    public RemoteNotYetReturnedException(final RemoteExecCommand theRemoteCommand) {
        super(theRemoteCommand.getCommand());
        remoteCommand = theRemoteCommand;
    }

    public RemoteNotYetReturnedException(final RemoteExecCommand theRemoteCommand,
                                         final Throwable theCause) {
        super(theRemoteCommand.getCommand(),
              theCause);
        remoteCommand = theRemoteCommand;
    }

    public RemoteExecCommand getRemoteCommand() {
        return remoteCommand;
    }
}
