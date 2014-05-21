package com.siemens.cto.aem.control.webserver.command.impl;

import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.control.command.PlatformCommandProvider;

import com.siemens.cto.aem.control.webserver.command.WebServerExecCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.platform.Platform;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;

public class DefaultWebServerExecCommandBuilderImpl implements WebServerExecCommandBuilder {

    private WebServer webServer;
    private WebServerControlOperation controlOperation;

    @Override
    public WebServerExecCommandBuilder setWebServer(final WebServer aWebServer) {
        webServer = aWebServer;
        return this;
    }

    @Override
    public WebServerExecCommandBuilder setOperation(final WebServerControlOperation anOperation) {
        controlOperation = anOperation;
        return this;
    }

    @Override
    public ExecCommand build() {
        //TODO The platform must come from the Web Server in the future (i.e. once it's ready and available)
        final PlatformCommandProvider provider = PlatformCommandProvider.lookup(Platform.WINDOWS);
        final ServiceCommandBuilder builder = provider.getServiceCommandBuilderFor(controlOperation);
        return builder.buildCommandForService(webServer.getName());
    }
}
