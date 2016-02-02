package com.siemens.cto.aem.control.application.command.impl;

import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.control.application.command.windows.WindowsApplicationNetOperation;
import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;

public class WindowsApplicationPlatformCommandProvider implements PlatformCommandProvider<ApplicationControlOperation> {

    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(ApplicationControlOperation anOperation) {
        return WindowsApplicationNetOperation.lookup(anOperation);
    }
}
