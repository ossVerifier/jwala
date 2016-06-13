package com.siemens.cto.aem.control.webserver.command.windows;

import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ShellCommand;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
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
    BACK_UP_HTTP_CONFIG_FILE(WebServerControlOperation.BACK_UP_CONFIG_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/cp", aParams[0], aParams[1]);
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
    INVOKE_SERVICE(WebServerControlOperation.INVOKE_SERVICE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(
                    cygpathWrapper(INVOKE_WS_SERVICE_SCRIPT_NAME, USER_TOC_SCRIPTS_PATH + "/"),
                    aServiceName,
                    WEBSERVER_CONF_PATH
            );
        }
    },
    CREATE_DIRECTORY(WebServerControlOperation.CREATE_DIRECTORY) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("if [ ! -e \"" + aParams[0] + "\" ]; then /usr/bin/mkdir -p " + aParams[0] + "; fi;");
        }
    },
    CHANGE_FILE_MODE(WebServerControlOperation.CHANGE_FILE_MODE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/chmod " + aParams[0] + " " + aParams[1] + "/" + aParams[2]);
        }
    },
    CHECK_FILE_EXISTS(WebServerControlOperation.CHECK_FILE_EXISTS) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/test -e " + aParams[0]);
        }
    };

    private static String cygpathWrapper(AemControl.Properties scriptName, String scriptPath) {
        return "`" + CYGPATH.toString() + " " + scriptPath + scriptName + "`";
    }

    private static final Map<WebServerControlOperation, WindowsWebServerNetOperation> LOOKUP_MAP = new EnumMap<>(WebServerControlOperation.class);

    public static final String HTTPD_CONF_PATH = ApplicationProperties.get("paths.httpd.conf");
    public static final String WEBSERVER_CONF_PATH = ApplicationProperties.get("remote.paths.httpd.conf", HTTPD_CONF_PATH);

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
