package com.siemens.cto.aem.control.configuration;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.med.hs.soarian.config.PropertiesStore;

@Configuration
public class AemSshConfig {

    private static final String AEM_SSH_PROPERTY_SET = "AemSsh";

    @Bean
    public SshConfiguration getSshConfiguration() {

        final Properties sshProperties = PropertiesStore.getProperties(AEM_SSH_PROPERTY_SET);

        final SshConfiguration configuration = new SshConfiguration(getStringPropertyFrom(sshProperties,
                                                                                          AemSshProperty.USER_NAME),
                                                                    getIntegerPropertyFrom(sshProperties,
                                                                                           AemSshProperty.PORT),
                                                                    getStringPropertyFrom(sshProperties,
                                                                                          AemSshProperty.PRIVATE_KEY_FILE),
                                                                    getStringPropertyFrom(sshProperties,
                                                                                          AemSshProperty.KNOWN_HOSTS_FILE));

        return configuration;
    }

    @Bean
    public JSch getJsch() {
        try {
            final SshConfiguration sshConfig = getSshConfiguration();
            final JschBuilder builder = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                                                         .setKnownHostsFileName(sshConfig.getKnownHostsFile());

            return builder.build();
        } catch (final JSchException jse) {
            throw new RuntimeException(jse);
        }
    }

    protected String getStringPropertyFrom(final Properties someProperties,
                                           final AemSshProperty aProperty) {
        return someProperties.getProperty(aProperty.getPropertyName());
    }

    protected Integer getIntegerPropertyFrom(final Properties someProperties,
                                             final AemSshProperty aProperty) {
        return Integer.valueOf(getStringPropertyFrom(someProperties,
                                                     aProperty));
    }
}
