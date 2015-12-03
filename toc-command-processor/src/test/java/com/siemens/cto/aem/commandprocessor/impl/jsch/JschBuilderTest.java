package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.impl.CommonSshTestConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JschBuilderTest {

    private String knownHostsFile;
    private String privateKeyFile;

    @Before
    public void setup() {
        final CommonSshTestConfiguration config = new CommonSshTestConfiguration();
        knownHostsFile = config.getKnownHostsFile();
        privateKeyFile = config.getPrivateKey();
    }

    @Test
    public void testKnownHostsFile() throws Exception {

        final JschBuilder builder = new JschBuilder();
        builder.setKnownHostsFileName(knownHostsFile);
        builder.setPrivateKeyFileName(privateKeyFile);

        final JSch jsch = builder.build();
        final HostKeyRepository knownHosts = jsch.getHostKeyRepository();

        assertEquals(knownHostsFile,
                     knownHosts.getKnownHostsRepositoryID());
    }
}
