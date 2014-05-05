package com.siemens.cto.aem.control.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.ThreadedCommandExecutorImpl;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.control.jvm.impl.RemoteJvmCommandExecutorImpl;

@Configuration
public class AemCommandExecutorConfig {

    @Autowired
    private AemSshConfig sshConfig;

    @Bean
    public JvmCommandExecutor getJvmCommandExecutor() {
        final JvmCommandExecutor jvmCommandExecutor = new RemoteJvmCommandExecutorImpl(getCommandExecutor(),
                                                                                       sshConfig.getJsch(),
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
        return Executors.newFixedThreadPool(12);
    }
}
