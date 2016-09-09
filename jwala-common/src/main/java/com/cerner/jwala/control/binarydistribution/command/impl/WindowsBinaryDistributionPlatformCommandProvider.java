package com.cerner.jwala.control.binarydistribution.command.impl;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.control.binarydistribution.command.windows.WindowsBinaryDistributionNetOperation;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

public class WindowsBinaryDistributionPlatformCommandProvider implements PlatformCommandProvider<BinaryDistributionControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(BinaryDistributionControlOperation anOperation) {
        return WindowsBinaryDistributionNetOperation.lookup(anOperation);
    }
}
