package com.siemens.cto.aem.control.configuration;

import com.siemens.cto.aem.commandprocessor.domain.RemoteSystemConnection;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;

public class CommonSshTestConfiguration {

    private static final String KNOWN_HOSTS_FILE = "aem-command-processor\\src\\test\\resources\\known_hosts";
    private static final String PRIVATE_KEY = "aem-command-processor\\src\\test\\resources\\test_private_key";

    private final JschBuilder builder;
    private final RemoteSystemConnection remoteSystemConnection;

    public CommonSshTestConfiguration() {
        //TODO generalize this so that %HOME% works
        //TODO create duplicate set of keys without a passphrase for testing
        builder = new JschBuilder(KNOWN_HOSTS_FILE,
                                  PRIVATE_KEY);
        remoteSystemConnection = new RemoteSystemConnection("z002xuvs",
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
}
