package com.cerner.jwala.control.binarydistribution.command.windows;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.command.ServiceCommandBuilder;

import java.util.EnumMap;
import java.util.Map;

import static com.cerner.jwala.control.AemControl.Properties.CYGPATH;
import static com.cerner.jwala.control.AemControl.Properties.SCP_SCRIPT_NAME;

public enum WindowsBinaryDistributionNetOperation implements ServiceCommandBuilder {

    CHECK_FILE_EXISTS(BinaryDistributionControlOperation.CHECK_FILE_EXISTS) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ShellCommand("/usr/bin/test -e " + aParams[0]);
        }
    },
    CREATE_DIRECTORY(BinaryDistributionControlOperation.CREATE_DIRECTORY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ShellCommand("if [ ! -e \"" + aParams[0] + "\" ]; then /usr/bin/mkdir -p " + aParams[0] + "; fi;");
        }
    },
    SECURE_COPY(BinaryDistributionControlOperation.SECURE_COPY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ShellCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    DELETE_BINARY(BinaryDistributionControlOperation.DELETE_BINARY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams){
            return new ShellCommand("/usr/bin/rm " + aParams[0]);
        }
    },
    UNZIP_BINARY(BinaryDistributionControlOperation.UNZIP_BINARY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ShellCommand(
                    aParams[0] + "  -q " + aParams[1] + " -d " + aParams[2]
            );
        }
    },
    CHANGE_FILE_MODE(BinaryDistributionControlOperation.CHANGE_FILE_MODE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String directory = aParams[1].replaceAll("\\\\","/");
            String cygwinDir = "`" + CYGPATH.toString() + " " + directory + "`";
            return new ShellCommand("/usr/bin/chmod " + aParams[0] + " " + cygwinDir + "/" + aParams[2]);
        }
    },;

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
