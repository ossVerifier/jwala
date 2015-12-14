package com.siemens.cto.aem.control.jvm.command.windows;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ShellCommand;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import static com.siemens.cto.aem.control.AemControl.Properties.*;

/**
 * Windows JVM Net Operations
 * <p>
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
            String dataJvmResourcesDir = ApplicationProperties.get("stp.jvm.resources.dir");
            String instancesDir = ApplicationProperties.get("paths.instances");
            return new ExecCommand(
                    cygpathWrapper(DEPLOY_CONFIG_TAR_SCRIPT_NAME),
                    dataJvmResourcesDir + "/" + aServiceName + "_config.tar",
                    instancesDir + "/" + aServiceName,
                    dataJvmResourcesDir
            );
        }
    },
    DELETE_SERVICE(JvmControlOperation.DELETE_SERVICE) {
        @Override
        public ExecCommand buildCommandForService(String aServiceName, String... aParams) {
            return new ExecCommand(
                    cygpathWrapper(DELETE_SERVICE_SCRIPT_NAME),
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

    private static String cygpathWrapper(AemControl.Properties scriptPath) {
        return "`" + CYGPATH.toString() + " " + SCRIPTS_PATH.toString() + scriptPath + "`";
    }
}