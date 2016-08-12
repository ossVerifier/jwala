package com.cerner.jwala.control.application.command.impl;

import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.control.application.command.windows.WindowsApplicationNetOperation;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

public class WindowsApplicationPlatformCommandProvider implements PlatformCommandProvider<ApplicationControlOperation> {

    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(ApplicationControlOperation anOperation) {
        return WindowsApplicationNetOperation.lookup(anOperation);
    }
}
