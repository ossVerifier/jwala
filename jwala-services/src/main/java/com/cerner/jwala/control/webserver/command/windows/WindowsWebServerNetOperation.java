package com.cerner.jwala.control.webserver.command.windows;

import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.cerner.jwala.control.AemControl.Properties.*;

public enum WindowsWebServerNetOperation implements ServiceCommandBuilder {

    START(WebServerControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            String remoteScriptPath = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
            return new ShellCommand(
                    cygpathWrapper(START_SCRIPT_NAME, remoteScriptPath + "/"),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue()
            );
        }
    },
    STOP(WebServerControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            String remoteScriptPath = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
            return new ShellCommand(
                    cygpathWrapper(STOP_SCRIPT_NAME, remoteScriptPath + "/"),
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
    SCP(WebServerControlOperation.SCP) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    BACK_UP_HTTP(WebServerControlOperation.BACK_UP) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(USR_BIN_MV, aParams[0], aParams[1]);
        }
    },
    DELETE_SERVICE(WebServerControlOperation.DELETE_SERVICE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(
                    "sc delete",
                    aServiceName
            );
        }
    },
    INSTALL_SERVICE(WebServerControlOperation.INSTALL_SERVICE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            String remoteScriptPath = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
            return new ExecCommand(
                    cygpathWrapper(INSTALL_SERVICE_WS_SERVICE_SCRIPT_NAME, remoteScriptPath + "/"),
                    aServiceName
            );
        }
    },
    CREATE_DIRECTORY(WebServerControlOperation.CREATE_DIRECTORY) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("if [ ! -e \"" + aParams[0] + "\" ]; then " + USR_BIN_MKDIR + " -p " + aParams[0] + "; fi;");
        }
    },
    MAKE_UNIX_EXEC(WebServerControlOperation.CHANGE_FILE_MODE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(USR_BIN_CHMOD + " " + aParams[0] + " " + aParams[1] + "/" + aParams[2]);
        }
    },
    CHECK_FILE_EXISTS(WebServerControlOperation.CHECK_FILE_EXISTS) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(USR_BIN_TEST + " -e " + aParams[0]);
        }
    };

    private static final String USR_BIN_CYGPATH = "/usr/bin/cygpath";

    private static String cygpathWrapper(AemControl.Properties scriptName, String scriptPath) {
        return "`" + USR_BIN_CYGPATH + " " + scriptPath + scriptName + "`";
    }

    private static final Map<WebServerControlOperation, WindowsWebServerNetOperation> LOOKUP_MAP = new EnumMap<>(WebServerControlOperation.class);

    private static final String USR_BIN_MV = "/usr/bin/mv";
    private static final String USR_BIN_MKDIR = "/usr/bin/mkdir";
    private static final String USR_BIN_CHMOD = "/usr/bin/chmod";
    private static final String USR_BIN_TEST = "/usr/bin/test";

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
