package com.siemens.cto.aem.domain.model.ssh;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SshConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String userName;
    private final Integer port;
    private final String privateKeyFile;
    private final String knownHostsFile;

    public SshConfiguration(final String theUserName,
                            final Integer thePort,
                            final String thePrivateKeyFile,
                            final String theKnownHostsFile) {
        userName = theUserName;
        port = thePort;
        privateKeyFile = thePrivateKeyFile;
        knownHostsFile = theKnownHostsFile;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getPort() {
        return port;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public String getKnownHostsFile() {
        return knownHostsFile;
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
        SshConfiguration rhs = (SshConfiguration) obj;
        return new EqualsBuilder()
                .append(this.userName, rhs.userName)
                .append(this.port, rhs.port)
                .append(this.privateKeyFile, rhs.privateKeyFile)
                .append(this.knownHostsFile, rhs.knownHostsFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(userName)
                .append(port)
                .append(privateKeyFile)
                .append(knownHostsFile)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userName", userName)
                .append("port", port)
                .append("privateKeyFile", privateKeyFile)
                .append("knownHostsFile", knownHostsFile)
                .toString();
    }
}
