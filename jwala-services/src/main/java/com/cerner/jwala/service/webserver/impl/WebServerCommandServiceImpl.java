package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.DefaultExecCommandBuilderImpl;
import com.cerner.jwala.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerService;

/**
 * Encapsulates non-state altering commands to a web server.
 *
 * Created by Jedd Cuison on 8/25/14.
 */
public class WebServerCommandServiceImpl implements WebServerCommandService {

    private final WebServerService webServerService;
    private final SshConfiguration sshConfig;

    private final RemoteCommandExecutorService remoteCommandExecutorService;

    public WebServerCommandServiceImpl(final WebServerService webServerService, final SshConfiguration sshConfig,
                                       final RemoteCommandExecutorService remoteCommandExecutorService) {
        this.webServerService = webServerService;
        this.sshConfig = sshConfig;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
    }

    @Override
    public CommandOutput getHttpdConf(final Identifier<WebServer> webServerId) throws CommandFailureException {
        final WebServer webServer = webServerService.getWebServer(webServerId);
        String httpdPath = ApplicationProperties.get("remote.paths.httpd.conf");
        final ExecCommand execCommand = createExecCommand(webServer, WebServerControlOperation.VIEW_HTTP_CONFIG_FILE,
                httpdPath + "/httpd.conf");
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                sshConfig.getEncryptedPassword(), webServer.getHost(), sshConfig.getPort()), execCommand);
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
