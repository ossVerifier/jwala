package com.cerner.jwala.control.jvm.command.windows;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.control.command.ServiceCommandBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.cerner.jwala.common.properties.PropertyKeys.REMOTE_JAWALA_DATA_DIR;
import static com.cerner.jwala.control.AemControl.Properties.*;

/**
 * Windows JVM Net Operations
 * <p/>
 * When calling out to a script located on the disk
 * the path is located from the vars.properties file
 */
public enum WindowsJvmNetOperation implements ServiceCommandBuilder {

    START(JvmControlOperation.START) {
        String remotePathsInstancesDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATH_INSTANCES_DIR);

        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            final String scriptAbsolutePath = remotePathsInstancesDir + "/" + aServiceName +
                    "/" + ApplicationProperties.getRequired(PropertyKeys.REMOTE_TOMCAT_DIR_NAME) + "/bin";
            return new ShellCommand(
                    cygpathWrapper(START_SCRIPT_NAME, scriptAbsolutePath),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue()
            );
        }
    },
    STOP(JvmControlOperation.STOP) {
        String remotePathsInstancesDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATH_INSTANCES_DIR);

        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            final String scriptAbsolutePath = remotePathsInstancesDir + "/" + aServiceName +
                    "/" + ApplicationProperties.getRequired(PropertyKeys.REMOTE_TOMCAT_DIR_NAME) + "/bin";
            return new ShellCommand(
                    cygpathWrapper(STOP_SCRIPT_NAME, scriptAbsolutePath),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue());

        }
    },
    THREAD_DUMP(JvmControlOperation.THREAD_DUMP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            String remoteJavaHome = ApplicationProperties.get(PropertyKeys.REMOTE_JAVA_HOME);
            String jStackCmd = remoteJavaHome + "/bin/jstack";
            return new ExecCommand(jStackCmd, "-l `sc queryex", aServiceName, "| grep PID | awk '{ print $3 }'`");
        }
    },
    HEAP_DUMP(JvmControlOperation.HEAP_DUMP) {
        @Override
        // TODO: Refactor since the heap dump shell command is now a series of commands rather than one single command with parameters.
        // Note: The heap dump creates the directories, executes an echo to mark the start, then executes the heap dump
        //       itself then an echo to mark the end of the heap dump command sequence.
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            String remoteJavaHome = ApplicationProperties.get(PropertyKeys.REMOTE_JAVA_HOME);
            String dataDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_JAWALA_DATA_DIR);
            Boolean dumpLiveEnabled = ApplicationProperties.getRequiredAsBoolean(PropertyKeys.JMAP_DUMP_LIVE_ENABLED);

            String jMapCmd = "echo '***heapdump-start***';" + USR_BIN_MKDIR + " -p " + dataDir + ";" +
                    remoteJavaHome + "/bin/jmap";
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
            String dumpFile = dataDir + "/heapDump." + StringUtils.replace(aServiceName, " ", "") + "." +
                    fmt.print(DateTime.now());
            String parameters = "-dump:" + (dumpLiveEnabled ? "live," : "") + "format=b,file=" + dumpFile + " `sc queryex " +
                    aServiceName + " | grep PID | awk '{ print $3 }'`;echo '***heapdump-end***'";
            return new ExecCommand(jMapCmd, parameters);
        }
    },
    DEPLOY_CONFIG_ARCHIVE(JvmControlOperation.DEPLOY_CONFIG_ARCHIVE) {
        String remotePathsInstancesDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATH_INSTANCES_DIR);

        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String remoteScriptDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
            final String remoteJavaHome = ApplicationProperties.get(PropertyKeys.REMOTE_JAVA_HOME);

            return new ExecCommand(
                    cygpathWrapper(DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME, remoteScriptDir + "/" + aServiceName + "/"),
                    remoteScriptDir + "/" + aServiceName + ".jar",
                    remotePathsInstancesDir + "/" + aServiceName,
                    remoteJavaHome + "/bin/jar"
            );

        }
    },
    DELETE_SERVICE(JvmControlOperation.DELETE_SERVICE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(
                    "sc delete",
                    aServiceName
            );
        }
    },
    INSTALL_SERVICE(JvmControlOperation.INSTALL_SERVICE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String userName;
            final String encryptedPassword;

            if (aParams.length >= 2) {
                userName = aParams[0];
                encryptedPassword = aParams[1];
            } else {
                userName = null;
                encryptedPassword = null;
            }

            String remoteScriptDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
            String remotePathsInstancesDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATH_INSTANCES_DIR);

            final String quotedUsername;
            if (userName != null && userName.length() > 0) {
                quotedUsername = "\"" + userName + "\"";
            } else {
                quotedUsername = "";
            }
            final String decryptedPassword = encryptedPassword != null && encryptedPassword.length() > 0 ? new DecryptPassword().decrypt(encryptedPassword) : "";
            List<String> formatStrings = Arrays.asList(cygpathWrapper(INSTALL_SERVICE_SCRIPT_NAME,
                    remoteScriptDir + "/" + aServiceName + "/"),
                    aServiceName,
                    remotePathsInstancesDir+"/"+ApplicationProperties.getRequired(PropertyKeys.REMOTE_TOMCAT_DIR_NAME));
            List<String> unformatStrings = Arrays.asList(quotedUsername, decryptedPassword);
            return new ExecCommand(
                    formatStrings,
                    unformatStrings);
        }
    },
    SCP(JvmControlOperation.SCP) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    BACK_UP(JvmControlOperation.BACK_UP) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/mv", aParams[0], aParams[1]);
        }
    },
    CREATE_DIRECTORY(JvmControlOperation.CREATE_DIRECTORY) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("if [ ! -e \"" + aParams[0] + "\" ]; then " + USR_BIN_MKDIR + " -p " + aParams[0] + "; fi;");
        }
    },
    CHANGE_FILE_MODE(JvmControlOperation.CHANGE_FILE_MODE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            final String directory = aParams[1].replaceAll("\\\\", "/");
            String cygwinDir = "`" + USR_BIN_CYGPATH + " " + directory + "`";
            return new ExecCommand(USR_BIN_CHMOD + " " + aParams[0] + " " + cygwinDir + "/" + aParams[2]);
        }
    },
    CHECK_FILE_EXISTS(JvmControlOperation.CHECK_FILE_EXISTS) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(USR_BIN_TEST + " -e " + aParams[0]);
        }
    };

    private static final Map<JvmControlOperation, WindowsJvmNetOperation> LOOKUP_MAP = new EnumMap<>(
            JvmControlOperation.class);


    private static final String USR_BIN_MKDIR = "/usr/bin/mkdir";
    private static final String USR_BIN_CYGPATH = "/usr/bin/cygpath";
    private static final String USR_BIN_CHMOD = "/usr/bin/chmod";
    private static final String USR_BIN_TEST = "/usr/bin/test";

    static {
        for (final WindowsJvmNetOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    private final JvmControlOperation operation;

    private WindowsJvmNetOperation(final JvmControlOperation theOperation) {
        operation = theOperation;
    }

    private static String quotedServiceName(final String aServiceName) {
        return "\"" + aServiceName + "\"";
    }

    public static WindowsJvmNetOperation lookup(final JvmControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }

    protected static String cygpathWrapper(AemControl.Properties scriptName, String scriptAbsolutePath) {
        return "`" + USR_BIN_CYGPATH + " " + scriptAbsolutePath + "/" + scriptName + "`";
    }
}