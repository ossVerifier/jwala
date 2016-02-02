package com.siemens.cto.aem.control.webserver.command.windows;

import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ShellCommand;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.siemens.cto.aem.control.AemControl.Properties.*;

public enum WindowsWebServerNetOperation implements ServiceCommandBuilder {

    START(WebServerControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            return new ShellCommand(
                    cygpathWrapper(START_SCRIPT_NAME),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue()
            );
        }
    },
    STOP(WebServerControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            return new ShellCommand(
                    cygpathWrapper(STOP_SCRIPT_NAME),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue());
        }
    },
    VIEW_HTTP_CONFIG_FILE(WebServerControlOperation.VIEW_HTTP_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            return new ExecCommand("cat", aParams[0]);
        }
    },
    DEPLOY_HTTP_CONFIG_FILE(WebServerControlOperation.DEPLOY_HTTP_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    BACK_UP_HTTP_CONFIG_FILE(WebServerControlOperation.BACK_UP_HTTP_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/cp", aParams[0], aParams[1]);
        }
    };

    private static String cygpathWrapper(AemControl.Properties scriptPath) {
        return "`" + CYGPATH.toString() + " " + SCRIPTS_PATH.toString() + scriptPath + "`";
    }

    private static final Map<WebServerControlOperation, WindowsWebServerNetOperation> LOOKUP_MAP = new EnumMap<>(WebServerControlOperation.class);

    static {
        for (final WindowsWebServerNetOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    private final WebServerControlOperation operation;

    WindowsWebServerNetOperation(final WebServerControlOperation theOperation) {
        operation = theOperation;
    }

    private static String quotedServiceName(final String aServiceName) {
        return "\"" + aServiceName + "\"";
    }

    public static WindowsWebServerNetOperation lookup(final WebServerControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }
}
