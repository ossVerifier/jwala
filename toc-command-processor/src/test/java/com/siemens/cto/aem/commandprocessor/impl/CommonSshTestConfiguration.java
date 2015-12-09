package com.siemens.cto.aem.commandprocessor.impl;

import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.exec.RemoteSystemConnection;

public class CommonSshTestConfiguration {

    private static final String KNOWN_HOSTS_FILE = "toc-command-processor\\src\\test\\resources\\known_hosts";
    private static final String PRIVATE_KEY = "toc-command-processor\\src\\test\\resources\\test_private_key";

    private final JschBuilder builder;
    private final RemoteSystemConnection remoteSystemConnection;
    private String password;

    public CommonSshTestConfiguration() {
        //TODO (Corey) generalize this so that %HOME% works
        //TODO create duplicate set of keys without a passphrase for testing
        builder = new JschBuilder(KNOWN_HOSTS_FILE,
                                  PRIVATE_KEY);
        remoteSystemConnection = new RemoteSystemConnection("z002xuvs",
                "ppp123",
                                                            "usmlvv1cto989",
                                                            22);
    }

    public JschBuilder getBuilder() {
        return builder;
    }

    public RemoteSystemConnection getRemoteSystemConnection() {
        return remoteSystemConnection;
    }

    public String getKnownHostsFile() {
        return KNOWN_HOSTS_FILE;
    }

    public String getPrivateKey() {
        return PRIVATE_KEY;
    }

    public String getPassword() {
        return remoteSystemConnection.getPassword();
    }
}
