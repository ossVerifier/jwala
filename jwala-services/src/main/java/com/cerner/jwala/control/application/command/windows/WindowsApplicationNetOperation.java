package com.cerner.jwala.control.application.command.windows;

import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.cerner.jwala.control.AemControl.Properties.SCP_SCRIPT_NAME;
import static com.cerner.jwala.control.AemControl.Properties.UNPACK_BINARY_SCRIPT_NAME;

public enum WindowsApplicationNetOperation implements ServiceCommandBuilder {

    SECURE_COPY(ApplicationControlOperation.SECURE_COPY) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String configFilePath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), configFilePath, destPath);
        }
    },
    BACK_UP_FILE(ApplicationControlOperation.BACK_UP_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String srcPath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand(USR_BIN_CP, srcPath, destPath);
        }
    },
    UNPACK(ApplicationControlOperation.UNPACK) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String appWarName = aParams[0];
            final String appWarsDirPath = aParams[1];
            final String javaHomePath = REMOTE_JAVA_HOME;
            final String unpackWarScriptPath = "`" + USR_BIN_CYGPATH + " " + REMOTE_COMMANDS_USER_SCRIPTS + "/" + UNPACK_BINARY_SCRIPT_NAME + "`";
            return new ExecCommand(unpackWarScriptPath, appWarsDirPath, javaHomePath, appWarName, aParams[2]);
        }
    },
    CREATE_DIRECTORY(ApplicationControlOperation.CREATE_DIRECTORY) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("if [ ! -e \"" + aParams[0] + "\" ]; then " + USR_BIN_MKDIR + " -p " + aParams[0] + "; fi;");
        }
    },
    CHANGE_FILE_MODE(ApplicationControlOperation.CHANGE_FILE_MODE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(USR_BIN_CHMOD + " " + aParams[0] + " " + aParams[1] + "/" + aParams[2]);
        }
    },
    CHECK_FILE_EXISTS(ApplicationControlOperation.CHECK_FILE_EXISTS) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(USR_BIN_TEST + " -e " + aParams[0]);
        }
    };

    private static final String USR_BIN_CP = "/usr/bin/cp";
    private static final String USR_BIN_MKDIR = "/usr/bin/mkdir";
    private static final String USR_BIN_CHMOD = "/usr/bin/chmod";
    private static final String USR_BIN_TEST = "/usr/bin/test";
    private static final String USR_BIN_CYGPATH = "/usr/bin/cygpath";

    //private static final String REMOTE_WEBAPPS_DIR = ApplicationProperties.get("remote.jwala.webapps.dir");
    private static final String REMOTE_JAVA_HOME = ApplicationProperties.get("remote.jwala.java.home");
    private static final String REMOTE_COMMANDS_USER_SCRIPTS = ApplicationProperties.get("remote.commands.user-scripts");

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
