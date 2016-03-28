package com.siemens.cto.aem.control.application.command.windows;

import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.siemens.cto.aem.control.AemControl.Properties.*;

public enum WindowsApplicationNetOperation implements ServiceCommandBuilder {

    DEPLOY_WAR(ApplicationControlOperation.DEPLOY_WAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String generatedTarPath = aParams[0];
            final String webAppsDirPath = aParams[1];
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), generatedTarPath, webAppsDirPath);
        }
    },
    DEPLOY_CONFIG_FILE(ApplicationControlOperation.DEPLOY_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String configFilePath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), configFilePath, destPath);
        }
    },
    BACK_UP_CONFIG_FILE(ApplicationControlOperation.BACK_UP_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String srcPath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand("/usr/bin/cp", srcPath, destPath);
        }
    },
    UNPACK_WAR(ApplicationControlOperation.UNPACK_WAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String appWarName = aParams[0];
            final String appWarsDirPath = ApplicationProperties.get("stp.webapps.dir");
            final String javaHomePath = ApplicationProperties.get("stp.java.home");
            return new ExecCommand(cygpathWrapper(UNPACK_WAR_SCRIPT_NAME), appWarsDirPath, javaHomePath, appWarName);
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

    protected static String cygpathWrapper(AemControl.Properties scriptPath) {
        return "`" + CYGPATH.toString() + " " + SCRIPTS_PATH.toString() + scriptPath + "`";
    }
}
