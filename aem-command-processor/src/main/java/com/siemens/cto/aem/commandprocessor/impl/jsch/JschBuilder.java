package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class JschBuilder {

    private String knownHostsFileName;
    private String privateKeyFileName;

    public JschBuilder() {
    }

    public JschBuilder(final String aKnownHostsFileName,
                       final String aPrivateKeyFileName) {
        knownHostsFileName = aKnownHostsFileName;
        privateKeyFileName = aPrivateKeyFileName;
    }

    public JschBuilder setKnownHostsFileName(final String aKnownHostsFileName) {
        knownHostsFileName = aKnownHostsFileName;
        return this;
    }

    public JschBuilder setPrivateKeyFileName(final String aPrivateKeyFileName) {
        privateKeyFileName = aPrivateKeyFileName;
        return this;
    }

    public JSch build() throws JSchException {
        final JSch jsch = new JSch();
        jsch.setKnownHosts(knownHostsFileName);
        jsch.addIdentity(privateKeyFileName);
        return jsch;
    }
}
