package com.cerner.jwala.control.command.impl;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.control.command.ServiceCommandBuilder;
import com.cerner.jwala.control.command.windows.WindowsBinaryDistributionNetOperation;
import com.cerner.jwala.control.command.PlatformCommandProvider;

public class WindowsBinaryDistributionPlatformCommandProvider implements PlatformCommandProvider<BinaryDistributionControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(BinaryDistributionControlOperation anOperation) {
        return WindowsBinaryDistributionNetOperation.lookup(anOperation);
    }
}
