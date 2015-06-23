package com.siemens.cto.aem.control.webserver.command.windows;

import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.exec.ShellCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;

import java.util.EnumMap;
import java.util.Map;

import static com.siemens.cto.aem.control.AemControl.Properties.*;

public enum WindowsWebServerNetOperation implements ServiceCommandBuilder {

    START(WebServerControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            return new ShellCommand(
                    cygpathWrapper(START_SCRIPT_NAME),
                    quotedServiceName(aServiceName)
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

    private WindowsWebServerNetOperation() {
        operation = null;
    }

    private WindowsWebServerNetOperation(final WebServerControlOperation theOperation) {
        operation = theOperation;
    }

    private static String quotedServiceName(final String aServiceName) {
        return "\"" + aServiceName + "\"";
    }

    public static WindowsWebServerNetOperation lookup(final WebServerControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }
}