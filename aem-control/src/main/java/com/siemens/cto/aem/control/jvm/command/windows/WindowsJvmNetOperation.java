package com.siemens.cto.aem.control.jvm.command.windows;

import java.util.EnumMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;

public enum WindowsJvmNetOperation implements ServiceCommandBuilder {

    START(JvmControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName) {
            return new ExecCommand("net", "start", quotedServiceName(aServiceName));
        }
    } ,
    STOP(JvmControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName) {
            return new ExecCommand("net", "stop", quotedServiceName(aServiceName));
        }
    },
    THREAD_DUMP(JvmControlOperation.THREAD_DUMP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName) {
//            return new ExecCommand("c:/cygwin/threaddump.sh");
            return new ExecCommand("D:/apache/java/jdk1.7.0_45/bin/jstack", "-F `sc queryex", aServiceName, "| grep PID | awk '{ print $3 }'`");
        }
    };

    private static final Map<JvmControlOperation, WindowsJvmNetOperation> LOOKUP_MAP = new EnumMap<>(JvmControlOperation.class);

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
