package com.cerner.jwala.control.jvm.command.impl;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.ServiceCommandBuilder;
import com.cerner.jwala.control.jvm.command.linux.LinuxJvmNetOperation;


public class LinuxJvmPlatformCommandProvider implements PlatformCommandProvider<JvmControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(final JvmControlOperation operation) {
        return LinuxJvmNetOperation.lookup(operation);
    }

}
