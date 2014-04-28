package com.siemens.cto.aem.exception;

public class RemoteCommandFailureException extends Exception {

    private final String user;
    private final String host;
    private final String port;
    private final String command;

    public RemoteCommandFailureException(final String theCommand,
                                         final String theUser,
                                         final String theHost,
                                         final String thePort,
                                         final Throwable theCause) {
        super(theCause);
        user = theUser;
        host = theHost;
        port = thePort;
        command = theCommand;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getCommand() {
        return command;
    }
}
