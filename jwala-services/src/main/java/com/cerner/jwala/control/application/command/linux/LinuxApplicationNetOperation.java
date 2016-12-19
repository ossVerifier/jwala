package com.cerner.jwala.control.application.command.linux;

import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.cerner.jwala.control.AemControl.Properties.SCP_SCRIPT_NAME;

public enum LinuxApplicationNetOperation implements ServiceCommandBuilder {

    SECURE_COPY(ApplicationControlOperation.SECURE_COPY) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String configFilePath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), configFilePath, destPath);
        }
    },
    BACK_UP(ApplicationControlOperation.BACK_UP) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String srcPath = aParams[0];
            final String destPath = aParams[1];
            return new ExecCommand(USR_BIN_MV, srcPath, destPath);
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

    private static final String USR_BIN_MV = "mv";
    private static final String USR_BIN_MKDIR = "mkdir";
    private static final String USR_BIN_CHMOD = "chmod";
    private static final String USR_BIN_TEST = "test";

    private static final Map<ApplicationControlOperation, LinuxApplicationNetOperation> LOOKUP_MAP = new EnumMap<>(
            ApplicationControlOperation.class);

    static {
        for (final LinuxApplicationNetOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    private final ApplicationControlOperation operation;

    LinuxApplicationNetOperation(final ApplicationControlOperation theOperation) {
        operation = theOperation;
    }

    public static LinuxApplicationNetOperation lookup(final ApplicationControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }

}
