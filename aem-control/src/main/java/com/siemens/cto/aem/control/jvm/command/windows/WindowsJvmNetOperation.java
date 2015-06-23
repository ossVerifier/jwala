package com.siemens.cto.aem.control.jvm.command.windows;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.exec.ShellCommand;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
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
                    quotedServiceName(aServiceName)
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