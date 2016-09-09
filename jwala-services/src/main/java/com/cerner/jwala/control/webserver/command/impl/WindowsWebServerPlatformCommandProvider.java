package com.cerner.jwala.control.webserver.command.impl;

import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.ServiceCommandBuilder;
import com.cerner.jwala.control.webserver.command.windows.WindowsWebServerNetOperation;

public class WindowsWebServerPlatformCommandProvider implements PlatformCommandProvider<WebServerControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(WebServerControlOperation anOperation) {
        return WindowsWebServerNetOperation.lookup(anOperation);
    }
}
