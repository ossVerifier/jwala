package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.control.command.DefaultExecCommandBuilderImpl;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.siemens.cto.aem.control.command.RemoteCommandProcessorBuilder;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

/**
 * Encapsulates non-state altering commands to a web server.
 *
 * Created by z003bpej on 8/25/14.
 */
public class WebServerCommandServiceImpl implements WebServerCommandService {

    private final WebServerService webServerService;
    private final CommandExecutor executor;
    private final JschBuilder jschBuilder;
    private final SshConfiguration sshConfig;
    private final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;
    
    public WebServerCommandServiceImpl(final WebServerService webServerService, final CommandExecutor executor,
                                       final JschBuilder jschBuilder, final SshConfiguration sshConfig,
                                       final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool) {
        this.webServerService = webServerService;
        this.executor = executor;
        this.jschBuilder = jschBuilder;
        this.sshConfig = sshConfig;
        this.channelPool = channelPool;
    }

    @Override
    public CommandOutput getHttpdConf(final Identifier<WebServer> webServerId) throws CommandFailureException {
        final WebServer aWebServer = webServerService.getWebServer(webServerId);
        final ExecCommand execCommand = createExecCommand(aWebServer, WebServerControlOperation.VIEW_HTTP_CONFIG_FILE,
                aWebServer.getHttpConfigFile().getUriPath());

        return executeCommand(aWebServer, execCommand);
    }

    private CommandOutput executeCommand(final WebServer webServer, final ExecCommand execCommand) throws CommandFailureException {
        try {
            final RemoteCommandProcessorBuilder processorBuilder = new RemoteCommandProcessorBuilder();
            processorBuilder.setCommand(execCommand).setHost(webServer.getHost()).setJsch(jschBuilder.build()).setSshConfig(sshConfig)
                    .setChannelPool(channelPool);

            return executor.execute(processorBuilder);
        } catch (final JSchException jsche) {
            throw new CommandFailureException(execCommand, jsche);
        }
    }

    private ExecCommand createExecCommand(final WebServer webServer, final WebServerControlOperation wsControlOp, final String... params) {

        final DefaultExecCommandBuilderImpl<WebServerControlOperation> builder = new DefaultExecCommandBuilderImpl<>();
        builder.setOperation(wsControlOp);
        builder.setEntityName(webServer.getName());
        for (String param : params) {
            builder.setParameter(param);
        }
        return builder.build(new WindowsWebServerPlatformCommandProvider());
    }

}
