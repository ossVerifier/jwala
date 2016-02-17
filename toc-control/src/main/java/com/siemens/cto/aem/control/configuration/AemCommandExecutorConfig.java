package com.siemens.cto.aem.control.configuration;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.ThreadedCommandExecutorImpl;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschChannelService;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.RemoteCommandExecutorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AemCommandExecutorConfig {

    @Autowired
    private AemSshConfig sshConfig;

    @Autowired
    private JschChannelService jschChannelService;

    @Bean
    protected CommandExecutor getCommandExecutor() {
        return new ThreadedCommandExecutorImpl();
    }

    @Bean(destroyMethod = "shutdownNow")
    protected ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("command.executor.fixed.thread.pool", "150")));
    }

    @Bean
    public RemoteCommandExecutorImpl getRemoteCommandExecutor() {
        return new RemoteCommandExecutorImpl(getCommandExecutor(), sshConfig.getJschBuilder(), sshConfig.getSshConfiguration(),
                jschChannelService);
    }

}
