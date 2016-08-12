package com.cerner.jwala.common.exec;

import java.io.Serializable;

public class RemoteSystemConnection implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String user;
    private final String host;
    private final Integer port;
    private final String password;

    public RemoteSystemConnection(final String theUser,
                                  final String thePassword,
                                  final String theHost,
                                  final Integer thePort) {
        user = theUser;
        host = theHost;
        port = thePort;
        password = thePassword;
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

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RemoteSystemConnection that = (RemoteSystemConnection) o;

        if (!user.equals(that.user)) {
            return false;
        }
        if (!host.equals(that.host)) {
            return false;
        }
        return port.equals(that.port);

    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RemoteSystemConnection{" +
                "user='" + user + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

}
