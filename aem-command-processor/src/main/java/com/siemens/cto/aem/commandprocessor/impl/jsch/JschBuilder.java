package com.siemens.cto.aem.commandprocessor.impl.jsch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class JschBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschBuilder.class);
    private String knownHostsFileName;
    private String privateKeyFileName;

    public JschBuilder() {
    }

    public JschBuilder(final String aKnownHostsFileName, final String aPrivateKeyFileName) {
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
        LOGGER.debug("Initializing JSch Logger");
        JSch.setLogger(new JschLogger());
        final JSch jsch = new JSch();
        jsch.setKnownHosts(knownHostsFileName);
        jsch.addIdentity(privateKeyFileName);
        return jsch;
    }
}
