package com.siemens.cto.aem.control.jvm.command.linux;

import com.siemens.cto.aem.control.command.ServiceCommandBuilder;
import com.siemens.cto.aem.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;

import java.util.EnumMap;
import java.util.Map;

public enum LinuxJvmInitDOperation implements ServiceCommandBuilder {

    START(JvmControlOperation.START) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String...aParams) {
            return new ExecCommand(initDPath(aServiceName), "start");
        }
    } ,
    STOP(JvmControlOperation.STOP) {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName, final String...aParams) {
            return new ExecCommand(initDPath(aServiceName), "stop");
        }
    };

    private static final Map<JvmControlOperation, LinuxJvmInitDOperation> LOOKUP_MAP = new EnumMap<>(JvmControlOperation.class);

    static {
        for (final LinuxJvmInitDOperation o : values()) {
            LOOKUP_MAP.put(o.operation, o);
        }
    }

    private final JvmControlOperation operation;

    private LinuxJvmInitDOperation(final JvmControlOperation theOperation) {
        operation = theOperation;
    }

    private static String initDPath(final String aServiceName) {
        return "/etc/init.d/" + aServiceName;
    }

    public static LinuxJvmInitDOperation lookup(final JvmControlOperation anOperation) {
        return LOOKUP_MAP.get(anOperation);
    }
}