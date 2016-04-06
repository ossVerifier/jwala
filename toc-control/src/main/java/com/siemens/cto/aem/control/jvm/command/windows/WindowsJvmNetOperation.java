package com.siemens.cto.aem.control.jvm.command.windows;

import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ShellCommand;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            return new ShellCommand(
                    cygpathWrapper(START_SCRIPT_NAME),
                    quotedServiceName(aServiceName),
                    SLEEP_TIME.getValue()
            );
        }
    },
    STOP(JvmControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            return new ShellCommand(
                    cygpathWrapper(STOP_SCRIPT_NAME),
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
        public ExecCommand buildCommandForService(final String aServiceName, final String... aParams) {
            final Properties properties = ApplicationProperties.getProperties();
            String jMapCmd = properties.getProperty("stp.java.home") + "/bin/jmap";
            String dataDir = properties.getProperty("stp.data.dir");
            final boolean dumpLiveEnabled = Boolean.parseBoolean(properties.getProperty("jmap.dump.live.enabled"));
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd-HHmmss.SSS");
            String dumpFile = dataDir + "/heapDump-" + aServiceName + "-" + fmt.print(new DateTime());
            String parameters = "-dump:" + (dumpLiveEnabled ? "live," : "") + "format=b,file=" + dumpFile + " `sc queryex " +
                    aServiceName + " | grep PID | awk '{ print $3 }'`";
            return new ExecCommand(jMapCmd, parameters);
        }
    },
    DEPLOY_CONFIG_TAR(JvmControlOperation.DEPLOY_CONFIG_TAR) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {

            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            final String instancesDir = ApplicationProperties.get("paths.instances");
            final String javaJarAbsolutePath = ApplicationProperties.get("stp.java.home") + "/bin/jar";

            final String destinationDir = instancesDir + "/" + aServiceName;
            final String destinationDirBackup = destinationDir + "." + dateFormat.format(new Date());
            final String jvmJarFileName = aServiceName + "_config.jar";
            final String jvmJarAbsolutePath = instancesDir + "/" + jvmJarFileName;

            final String exitWithError = "echo 'EXIT_CODE='1***; echo -n -e '\\xff';";

            return new ExecCommand( new MessageFormat("if [ ! -e \"{0}\" ]; then echo Could not deploy {1}. No such directory {0}; {2} fi;").format(new String[]{instancesDir, jvmJarFileName, exitWithError}),
                                    new MessageFormat("if [ ! -e \"{0}\" ]; then echo Could not deploy {1}. Jar file does not exist at {0}; {2} fi;").format(new String[]{jvmJarAbsolutePath, jvmJarAbsolutePath, exitWithError}),
                                    new MessageFormat("if [ ! -e \"{0}\" ]; then echo Could not deploy {1}. No jar executable at {0}; {2} fi;").format(new String[]{javaJarAbsolutePath, jvmJarFileName, exitWithError}),
                                    new MessageFormat("cd {0};").format(new String[]{instancesDir}),
                                    new MessageFormat("if [ -e \"{0}\" ]; then /usr/bin/mv {0} {1}; fi;").format(new String[]{destinationDir, destinationDirBackup}),
                                    new MessageFormat("/usr/bin/mkdir {0};").format(new String[]{destinationDir}),
                                    new MessageFormat("{0} xf {1};").format(new String[]{javaJarAbsolutePath, jvmJarFileName}),
                                    new MessageFormat("/usr/bin/rm {0};").format(new String[]{jvmJarFileName}));
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
                    cygpathWrapper(INVOKE_SERVICE_SCRIPT_NAME),
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
    };

    private static final Map<JvmControlOperation, WindowsJvmNetOperation> LOOKUP_MAP = new EnumMap<>(
            JvmControlOperation.class);

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

    protected static String cygpathWrapper(AemControl.Properties scriptPath) {
        return "`" + CYGPATH.toString() + " " + SCRIPTS_PATH.toString() + scriptPath + "`";
    }
}