package com.siemens.cto.aem.control.configuration;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.ThreadedCommandExecutorImpl;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.control.jvm.impl.RemoteJvmCommandExecutorImpl;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.control.webserver.impl.RemoteWebServerCommandExecutorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AemCommandExecutorConfig {

    @Autowired
    private AemSshConfig sshConfig;

    @Bean
    public JvmCommandExecutor getJvmCommandExecutor() {
        final JvmCommandExecutor jvmCommandExecutor = new RemoteJvmCommandExecutorImpl(getCommandExecutor(),
                                                                                       sshConfig.getJschBuilder(),
                                                                                       sshConfig.getSshConfiguration());
        return jvmCommandExecutor;
    }

    @Bean
    protected CommandExecutor getCommandExecutor() {
        final CommandExecutor executor = new ThreadedCommandExecutorImpl(getExecutorService());
        return executor;
    }

    @Bean(destroyMethod = "shutdownNow")
    protected ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("command.executor.fixed.thread.pool", "150")));
    }

    @Bean
    public WebServerCommandExecutor getWebServerCommandExecutor() {
        final WebServerCommandExecutor webServerCommandExecutor =
                new RemoteWebServerCommandExecutorImpl(getCommandExecutor(),
                                                       sshConfig.getJschBuilder(),
                                                       sshConfig.getSshConfiguration());
        return webServerCommandExecutor;
    }
}
