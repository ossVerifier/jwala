package com.siemens.cto.aem.control.application.command.windows;

import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;

public enum WindowsApplicationNetOperation implements ServiceCommandBuilder {

    DEPLOY_WAR(ApplicationControlOperation.DEPLOY_WAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    DEPLOY_CONFIG_FILE(ApplicationControlOperation.DEPLOY_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    BACK_UP_CONFIG_FILE(ApplicationControlOperation.BACK_UP_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/cp", aParams[0], aParams[1]);
        }
    };

    private static final Map<ApplicationControlOperation, WindowsApplicationNetOperation> LOOKUP_MAP = new EnumMap<>(
            ApplicationControlOperation.class);

    static {
        for (final WindowsApplicationNetOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    private final ApplicationControlOperation operation;

    WindowsApplicationNetOperation(final ApplicationControlOperation theOperation) {
        operation = theOperation;
    }

    public static WindowsApplicationNetOperation lookup(final ApplicationControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }

}
