package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.Channel;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exec.*;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.DefaultExecCommandBuilderImpl;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
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

    private final RemoteCommandExecutorService remoteCommandExecutorService;

    public WebServerCommandServiceImpl(final WebServerService webServerService, final CommandExecutor executor,
                                       final JschBuilder jschBuilder, final SshConfiguration sshConfig,
                                       final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool,
                                       final RemoteCommandExecutorService remoteCommandExecutorService) {
        this.webServerService = webServerService;
        this.executor = executor;
        this.jschBuilder = jschBuilder;
        this.sshConfig = sshConfig;
        this.channelPool = channelPool;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
    }

    @Override
    public CommandOutput getHttpdConf(final Identifier<WebServer> webServerId) throws CommandFailureException {
        final WebServer webServer = webServerService.getWebServer(webServerId);
        String httpdPath = ApplicationProperties.get("remote.paths.httpd.conf", ApplicationProperties.get("paths.httpd.conf"));
        final ExecCommand execCommand = createExecCommand(webServer, WebServerControlOperation.VIEW_HTTP_CONFIG_FILE,
                httpdPath + "/httpd.conf");
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                sshConfig.getPassword(), webServer.getHost(), sshConfig.getPort()), execCommand);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = remoteCommandExecutorService.executeCommand(remoteExecCommand);

        return new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode), remoteCommandReturnInfo.standardOuput,
                remoteCommandReturnInfo.errorOupout);
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
