package com.cerner.jwala.control.jvm.command.impl;

import static com.cerner.jwala.control.AemControl.Properties.SCP_SCRIPT_NAME;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.ServiceCommandBuilder;
import com.cerner.jwala.control.jvm.command.windows.WindowsJvmNetOperation;

public class WindowsJvmPlatformCommandProvider implements PlatformCommandProvider<JvmControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(final JvmControlOperation operation) {
        return WindowsJvmNetOperation.lookup(operation);
    }

}
