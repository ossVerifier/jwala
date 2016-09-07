package com.cerner.jwala.control.binarydistribution.command.impl;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.control.binarydistribution.command.windows.WindowsBinaryDistributionNetOperation;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

/**
 * Created by LW044480 on 9/7/2016.
 */
public class WindowsBinaryDistributionPlatformCommandProvider implements PlatformCommandProvider<BinaryDistributionControlOperation> {
    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(BinaryDistributionControlOperation anOperation) {
        return WindowsBinaryDistributionNetOperation.lookup(anOperation);
    }
}
