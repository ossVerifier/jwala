package com.siemens.cto.aem.control.jvm.command.impl;

import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.control.jvm.command.windows.WindowsJvmNetOperation;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;

public class WindowsJvmPlatformCommandProvider implements PlatformCommandProvider<JvmControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(final JvmControlOperation operation) {
        return WindowsJvmNetOperation.lookup(operation);
    }

}
