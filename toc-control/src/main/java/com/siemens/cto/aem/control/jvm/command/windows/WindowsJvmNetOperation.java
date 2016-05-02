package com.siemens.cto.aem.control.jvm.command.windows;

import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ShellCommand;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import static com.siemens.cto.aem.control.AemControl.Properties.*;

/**
 * Windows JVM Net Operations
 * <p/>
 * When calling out to a script located on the disk
 * the path is located from the toc.properties file
 */
public enum WindowsJvmNetOperation implements ServiceCommandBuilder {

    START(JvmControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            final String scriptAbsolutePath = ApplicationProperties.get("commands.jvm.start.stop.path.scripts-path", INSTANCES_DIR + "/" + aServiceName + "/bin");
            return new ShellCommand(
                    cygpathWrapper(START_SCRIPT_NAME, scriptAbsolutePath),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue()
            );
        }
    },
    STOP(JvmControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            final String scriptAbsolutePath = ApplicationProperties.get("commands.jvm.start.stop.path.scripts-path", INSTANCES_DIR + "/" + aServiceName + "/bin");
            return new ShellCommand(
                    cygpathWrapper(STOP_SCRIPT_NAME, scriptAbsolutePath),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue());

        }
    },
    THREAD_DUMP(JvmControlOperation.THREAD_DUMP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            final Properties properties = ApplicationProperties.getProperties();
            String jStackCmd = properties.getProperty("stp.java.home") + "/bin/jstack";
            return new ExecCommand(jStackCmd, "-l `sc queryex", aServiceName, "| grep PID | awk '{ print $3 }'`");
        }
    },
    HEAP_DUMP(JvmControlOperation.HEAP_DUMP) {
        @Override
        // TODO: Refactor since the heap dump shell command is now a series of commands rather than one single command with parameters.
        // Note: The heap dump creates the directories, executes an echo to mark the start, then executes the heap dump
        //       itself then an echo to mark the end of the heap dump command sequence.
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            final Properties properties = ApplicationProperties.getProperties();
            String dataDir = properties.getProperty("stp.data.dir");
            String jMapCmd = "echo '***heapdump-start***';mkdir -p " + dataDir + ";" +
                    properties.getProperty("stp.java.home") + "/bin/jmap";
            final boolean dumpLiveEnabled = Boolean.parseBoolean(properties.getProperty("jmap.dump.live.enabled"));
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
            String dumpFile = dataDir + "/heapDump." + StringUtils.replace(aServiceName, " ", "") + "." +
                    fmt.print(DateTime.now());
            String parameters = "-dump:" + (dumpLiveEnabled ? "live," : "") + "format=b,file=" + dumpFile + " `sc queryex " +
                    aServiceName + " | grep PID | awk '{ print $3 }'`;echo '***heapdump-end***'";
            return new ExecCommand(jMapCmd, parameters);
        }
    },
    DEPLOY_CONFIG_TAR(JvmControlOperation.DEPLOY_CONFIG_TAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {

            String dataJvmResourcesDir = ApplicationProperties.get("stp.jvm.resources.dir");
            String instancesDir = ApplicationProperties.get("paths.instances");
            return new ExecCommand(
                    cygpathWrapper(DEPLOY_CONFIG_TAR_SCRIPT_NAME, USER_TOC_SCRIPTS_PATH + "/"),
                    USER_TOC_SCRIPTS_PATH + "/" + aServiceName + "_config.jar",
                    instancesDir + "/" + aServiceName,
                    dataJvmResourcesDir,
                    ApplicationProperties.get("stp.java.home") + "/bin/jar"
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
    INVOKE_SERVICE(JvmControlOperation.INVOKE_SERVICE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(
                    cygpathWrapper(INVOKE_SERVICE_SCRIPT_NAME, USER_TOC_SCRIPTS_PATH + "/"),
                    aServiceName,
                    ApplicationProperties.get("paths.instances")
            );
        }
    },
    SECURE_COPY(JvmControlOperation.SECURE_COPY) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(SCP_SCRIPT_NAME.getValue(), aParams[0], aParams[1]);
        }
    },
    BACK_UP_FILE(JvmControlOperation.BACK_UP_FILE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/cp", aParams[0], aParams[1]);
        }
    },
    CREATE_DIRECTORY(JvmControlOperation.CREATE_DIRECTORY){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("if [ ! -e \"" + aParams[0] + "\" ]; then /usr/bin/mkdir -p " + aParams[0] + "; fi;");
        }
    },
    CHANGE_FILE_MODE(JvmControlOperation.CHANGE_FILE_MODE){
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand("/usr/bin/chmod " + aParams[0] + " " + aParams[1] + "/" + aParams[2]);
        }
    };

    private static final Map<JvmControlOperation, WindowsJvmNetOperation> LOOKUP_MAP = new EnumMap<>(
            JvmControlOperation.class);

    public static final String INSTANCES_DIR = ApplicationProperties.get("paths.instances");

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
        return "`" + CYGPATH.toString() + " " + scriptAbsolutePath + "/" + scriptName + "`";
    }
}