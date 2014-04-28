package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class JSchBuilder {

    private String knownHostsFileName;
    private String privateKeyFileName;

    public JSchBuilder() {
    }

    public JSchBuilder(final String aKnownHostsFileName,
                       final String aPrivateKeyFileName) {
        knownHostsFileName = aKnownHostsFileName;
        privateKeyFileName = aPrivateKeyFileName;
    }

    public JSchBuilder setKnownHostsFileName(final String aKnownHostsFileName) {
        knownHostsFileName = aKnownHostsFileName;
        return this;
    }

    public JSchBuilder setPrivateKeyFileName(final String aPrivateKeyFileName) {
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
