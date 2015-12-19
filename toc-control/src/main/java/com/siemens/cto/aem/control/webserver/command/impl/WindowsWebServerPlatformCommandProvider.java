package com.siemens.cto.aem.control.webserver.command.impl;

import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.control.webserver.command.windows.WindowsWebServerNetOperation;

public class WindowsWebServerPlatformCommandProvider implements PlatformCommandProvider<WebServerControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(WebServerControlOperation anOperation) {
        return WindowsWebServerNetOperation.lookup(anOperation);
    }
}
