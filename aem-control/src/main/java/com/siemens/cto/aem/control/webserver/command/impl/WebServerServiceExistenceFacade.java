package com.siemens.cto.aem.control.webserver.command.impl;

import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.platform.Platform;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

public class WebServerServiceExistenceFacade {

    public ExecCommand getServiceExistenceCommandFor(final WebServer aWebServer) {
        //TODO This should come from a property on WebServer once it's ready
        final PlatformCommandProvider provider = PlatformCommandProvider.lookup(Platform.WINDOWS);
        //TODO If the WindowsGenericNonControlOperation ever has anything more than just QUERY_EXISTENCE, it will need to be accounted for here (among other places)
        return provider.getGenericServiceCommandBuilder().buildCommandForService(aWebServer.getName());
    }
}
