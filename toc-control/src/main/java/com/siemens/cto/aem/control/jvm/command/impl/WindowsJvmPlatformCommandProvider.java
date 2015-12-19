package com.siemens.cto.aem.control.jvm.command.impl;

import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.control.jvm.command.windows.WindowsJvmNetOperation;

public class WindowsJvmPlatformCommandProvider implements PlatformCommandProvider<JvmControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(final JvmControlOperation operation) {
        return WindowsJvmNetOperation.lookup(operation);
    }

}
