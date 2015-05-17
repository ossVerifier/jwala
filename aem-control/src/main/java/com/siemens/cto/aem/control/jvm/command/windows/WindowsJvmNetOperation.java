package com.siemens.cto.aem.control.jvm.command.windows;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;

public enum WindowsJvmNetOperation implements ServiceCommandBuilder {

    START(JvmControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String...aParams) {
            return new ExecCommand("net", "start", quotedServiceName(aServiceName));
        }
    },
    STOP(JvmControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String...aParams) {
            //return new ExecCommand("net", "stop", quotedServiceName(aServiceName));
            String sleepSecStr = ApplicationProperties.get("net.stop.sleep.time.seconds", NET_STOP_SLEEP_TIME_SECONDS_DEFAULT);
            return new ExecCommand(
                    "export B=`", 
                        "sc queryex ", quotedServiceName(aServiceName), 
                            "| grep PID ",
                            "| awk '{ print $3 }'",
                        "`;",
                    "if [ $B -ne 0 ]; then ",
                        "sc stop ",quotedServiceName(aServiceName),"> /dev/null; ",
                        "export A=$? ",                 "; ",
                        "/usr/bin/sleep ",sleepSecStr,  "; ",
                        "export C=`", 
                        "sc queryex ", quotedServiceName(aServiceName), 
                            "| grep PID ",
                            "| awk '{ print $3 }'",
                        "`;",
                        "if [ $C -ne $B ]; then ",
                            "exit $A ",                 "; ",
                        "fi",                           "; ",
                        "echo Service TERMINATED.",     "; ",
                        "echo ./toc-mcast TERMINATED ", quotedServiceName(aServiceName),"; ",
                        "( sc query ",quotedServiceName(aServiceName),"| tail -8 )",    "; ",                        
                        "/usr/bin/kill -9 -f $B ",      "; ",
                        "exit " + ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS,                      "; ",
                    "else ",
                        "echo The service has not been started.",                       "; ",
                        "exit " + ExecReturnCode.STP_EXIT_CODE_NO_OP,                                 "; ",
                    "fi");
        }
    },
    THREAD_DUMP(JvmControlOperation.THREAD_DUMP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String...aParams) {
            final Properties properties = ApplicationProperties.getProperties();
            String jStackCmd = properties.getProperty("stp.java.home") + "/bin/jstack";
            return new ExecCommand(jStackCmd, "-l `sc queryex", aServiceName, "| grep PID | awk '{ print $3 }'`");
        }
    },
    HEAP_DUMP(JvmControlOperation.HEAP_DUMP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String...aParams) {
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

    private static final String NET_STOP_SLEEP_TIME_SECONDS_DEFAULT = "60";
    
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
}