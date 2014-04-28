package com.siemens.cto.aem.commandprocessor.domain;

import java.io.Serializable;

public class RemoteExecCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RemoteSystemConnection remoteSystemConnection;
    private final ExecCommand command;

    public RemoteExecCommand(final RemoteSystemConnection theRemoteSystemConnection,
                             final ExecCommand theCommand) {
        remoteSystemConnection = theRemoteSystemConnection;
        command = theCommand;
    }

    public RemoteSystemConnection getRemoteSystemConnection() {
        return remoteSystemConnection;
    }

    public ExecCommand getCommand() {
        return command;
    }
}
