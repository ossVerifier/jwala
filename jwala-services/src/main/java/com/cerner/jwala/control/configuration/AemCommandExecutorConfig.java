package com.cerner.jwala.control.configuration;

import com.cerner.jwala.commandprocessor.CommandExecutor;
import com.cerner.jwala.commandprocessor.impl.CommandExecutorImpl;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.RemoteCommandExecutorImpl;
import com.jcraft.jsch.Channel;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
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
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Bean
    protected CommandExecutor getCommandExecutor() {
        return new CommandExecutorImpl();
    }

    @Bean(destroyMethod = "shutdownNow")
    protected ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("command.executor.fixed.thread.pool", "150")));
    }

    @Bean
    public RemoteCommandExecutorImpl getRemoteCommandExecutor() {
        return new RemoteCommandExecutorImpl(getCommandExecutor(), sshConfig.getJschBuilder(), sshConfig.getSshConfiguration(),
                channelPool);
    }

}
