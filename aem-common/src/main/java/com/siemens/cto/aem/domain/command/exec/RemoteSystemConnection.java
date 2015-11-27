package com.siemens.cto.aem.domain.command.exec;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        RemoteSystemConnection rhs = (RemoteSystemConnection) obj;
        return new EqualsBuilder()
                .append(this.user, rhs.user)
                .append(this.host, rhs.host)
                .append(this.port, rhs.port)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(user)
                .append(host)
                .append(port)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("user", user)
                .append("host", host)
                .append("port", port)
                .toString();
    }
}
