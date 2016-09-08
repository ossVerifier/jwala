package com.cerner.jwala.control.binarydistribution.command.windows;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.cerner.jwala.control.AemControl.Properties.*;

public enum WindowsBinaryDistributionNetOperation implements ServiceCommandBuilder {

    CHECK_FILE_EXISTS(BinaryDistributionControlOperation.CHECK_FILE_EXISTS) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/test -e " + aParams[0]);
        }
    },
    CREATE_DIRECTORY(BinaryDistributionControlOperation.CREATE_DIRECTORY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("if [ ! -e \"" + aParams[0] + "\" ]; then /usr/bin/mkdir -p " + aParams[0] + "; fi;");
        }
    },
    SECURE_COPY(BinaryDistributionControlOperation.SECURE_COPY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    DELETE_BINARY(BinaryDistributionControlOperation.DELETE_BINARY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams){
            return new ExecCommand("/usr/bin/rm " + aParams[0]);
        }
    },
    UNZIP_BINARY(BinaryDistributionControlOperation.UNZIP_BINARY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(
                    cygpathWrapper(DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME, USER_TOC_SCRIPTS_PATH + "/"),
                    aParams[0],
                    ApplicationProperties.get("stp.java.home") + "/bin/jar xf"
            );
        }
    };

    private final BinaryDistributionControlOperation operation;

    WindowsBinaryDistributionNetOperation(BinaryDistributionControlOperation operation) {
        this.operation = operation;
    }

    @Override
    public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
        return null;
    }

    private static final Map<BinaryDistributionControlOperation, WindowsBinaryDistributionNetOperation> LOOKUP_MAP = new EnumMap<>(
            BinaryDistributionControlOperation.class);

    public static WindowsBinaryDistributionNetOperation lookup(final BinaryDistributionControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }

    static {
        for (final WindowsBinaryDistributionNetOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    protected static String cygpathWrapper(AemControl.Properties scriptName, String scriptAbsolutePath) {
        return "`" + CYGPATH.toString() + " " + scriptAbsolutePath + "/" + scriptName + "`";
    }
}
