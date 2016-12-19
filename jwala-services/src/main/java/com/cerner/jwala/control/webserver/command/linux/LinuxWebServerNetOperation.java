package com.cerner.jwala.control.webserver.command.linux;

import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.cerner.jwala.control.AemControl.Properties.*;

public enum LinuxWebServerNetOperation implements ServiceCommandBuilder {

    START(WebServerControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            return new ShellCommand(
                    cygpathWrapper(START_SCRIPT_NAME, WEBSERVER_CONF_PATH + "/"),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue()
            );
        }
    },
    STOP(WebServerControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            return new ShellCommand(
                    cygpathWrapper(STOP_SCRIPT_NAME, WEBSERVER_CONF_PATH + "/"),
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
    SECURE_COPY(WebServerControlOperation.SECURE_COPY) {
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
            return new ExecCommand(
                    cygpathWrapper(INSTALL_SERVICE_WS_SERVICE_SCRIPT_NAME, REMOTE_COMMANDS_USER_SCRIPTS + "/"),
                    aServiceName,
                    WEBSERVER_CONF_PATH
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

    private static String cygpathWrapper(AemControl.Properties scriptName, String scriptPath) {
        return "`" + scriptPath + scriptName + "`";
    }

    private static final Map<WebServerControlOperation, LinuxWebServerNetOperation> LOOKUP_MAP = new EnumMap<>(WebServerControlOperation.class);

    public static final String WEBSERVER_CONF_PATH = ApplicationProperties.get("remote.paths.httpd.conf");
    private static final String REMOTE_COMMANDS_USER_SCRIPTS = ApplicationProperties.get("remote.commands.user-scripts");
    private static final String USR_BIN_MV = "mv";
    private static final String USR_BIN_MKDIR = "mkdir";
    private static final String USR_BIN_CHMOD = "chmod";
    private static final String USR_BIN_TEST = "test";

    static {
        for (final LinuxWebServerNetOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    private final WebServerControlOperation operation;

    LinuxWebServerNetOperation(final WebServerControlOperation theOperation) {
        operation = theOperation;
    }

    private static String quotedServiceName(final String aServiceName) {
        return "\"" + aServiceName + "\"";
    }

    public static LinuxWebServerNetOperation lookup(final WebServerControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }
}
