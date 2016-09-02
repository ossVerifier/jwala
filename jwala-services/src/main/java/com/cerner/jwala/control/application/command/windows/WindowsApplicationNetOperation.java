package com.cerner.jwala.control.application.command.windows;

import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

import static com.cerner.jwala.control.AemControl.Properties.*;

import java.util.EnumMap;
import java.util.Map;

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
            return new ExecCommand("/usr/bin/cp", srcPath, destPath);
        }
    },
    UNPACK_WAR(ApplicationControlOperation.UNPACK_WAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String appWarName = aParams[0];
            final String appWarsDirPath = ApplicationProperties.get("stp.webapps.dir");
            final String javaHomePath = ApplicationProperties.get("stp.java.home");
            final String unpackWarScriptPath = "`" + CYGPATH + " " + USER_JWALA_SCRIPTS_PATH + "/" + UNPACK_WAR_SCRIPT_NAME + "`";
            return new ExecCommand(unpackWarScriptPath, appWarsDirPath, javaHomePath, appWarName);
        }
    },
    CREATE_DIRECTORY(ApplicationControlOperation.CREATE_DIRECTORY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("if [ ! -e \"" + aParams[0] + "\" ]; then /usr/bin/mkdir -p " + aParams[0] + "; fi;");
        }
    },
    CHANGE_FILE_MODE(ApplicationControlOperation.CHANGE_FILE_MODE){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/chmod " + aParams[0] + " " + aParams[1] + "/" + aParams[2]);
        }
    },
    CHECK_FILE_EXISTS(ApplicationControlOperation.CHECK_FILE_EXISTS){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/test -e " + aParams[0]);
        }
    }
    ;

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
