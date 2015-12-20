package com.siemens.cto.aem.control.application.command.impl;

import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;

public class WindowsApplicationPlatformCommandProvider implements PlatformCommandProvider<ApplicationControlOperation> {

    @Override
    public ServiceCommandBuilder getServiceCommandBuilderFor(ApplicationControlOperation anOperation) {
        return new ServiceCommandBuilder() {
            @Override
            public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
                return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
            }
        };
    }
}
