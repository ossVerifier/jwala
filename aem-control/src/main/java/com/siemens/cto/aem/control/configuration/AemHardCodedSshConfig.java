package com.siemens.cto.aem.control.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;

@Configuration
public class AemHardCodedSshConfig extends AemSshConfig {

    @Bean
    @Override
    public SshConfiguration getSshConfiguration() {

        final SshConfiguration configuration = new SshConfiguration("z002xuvs",
                                                                    22,
                                                                    getPrivateKeyFile(),
                                                                    getKnownHostsFile());

        return configuration;
    }

    protected String getSshFolderRoot() {
        return System.getProperty("PROPERTIES_ROOT_PATH");
    }

    protected String getPrivateKeyFile() {
        return getSshFolderRoot() + "/" + "test_private_key";
    }

    protected String getKnownHostsFile() {
        return getSshFolderRoot() + "/" + "known_hosts";
    }
}
