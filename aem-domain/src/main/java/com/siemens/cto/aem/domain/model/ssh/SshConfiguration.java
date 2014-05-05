package com.siemens.cto.aem.domain.model.ssh;

import java.io.Serializable;

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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SshConfiguration that = (SshConfiguration) o;

        if (knownHostsFile != null ? !knownHostsFile.equals(that.knownHostsFile) : that.knownHostsFile != null) {
            return false;
        }
        if (port != null ? !port.equals(that.port) : that.port != null) {
            return false;
        }
        if (privateKeyFile != null ? !privateKeyFile.equals(that.privateKeyFile) : that.privateKeyFile != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (privateKeyFile != null ? privateKeyFile.hashCode() : 0);
        result = 31 * result + (knownHostsFile != null ? knownHostsFile.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SshConfiguration{" +
               "userName='" + userName + '\'' +
               ", port=" + port +
               ", privateKeyFile='" + privateKeyFile + '\'' +
               ", knownHostsFile='" + knownHostsFile + '\'' +
               '}';
    }
}
