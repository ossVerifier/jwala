package com.siemens.cto.aem.commandprocessor.domain;

import java.io.Serializable;

public class RemoteSystemConnection implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String user;
    private final String host;
    private final Integer port;

    public RemoteSystemConnection(final String theUser,
                                  final String theHost,
                                  final Integer thePort) {
        user = theUser;
        host = theHost;
        port = thePort;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }
}
