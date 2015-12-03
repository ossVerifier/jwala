package com.siemens.cto.aem.control.command;

import com.siemens.cto.aem.control.jvm.command.windows.WindowsJvmNetOperation;
import com.siemens.cto.aem.control.webserver.command.windows.WindowsWebServerNetOperation;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.platform.Platform;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;

import java.util.EnumMap;
import java.util.Map;

public enum PlatformCommandProvider {

    WINDOWS(Platform.WINDOWS) {
        @Override
        public ServiceCommandBuilder getServiceCommandBuilderFor(final JvmControlOperation anOperation) {
            return WindowsJvmNetOperation.lookup(anOperation);
        }

        @Override
        public ServiceCommandBuilder getServiceCommandBuilderFor(final WebServerControlOperation anOperation) {
            return WindowsWebServerNetOperation.lookup(anOperation);
        }

        @Override
        public ServiceCommandBuilder getGenericServiceCommandBuilder() {
            return WindowsGenericNonControlOperation.QUERY_SERVICE_EXISTENCE;
        }

    }; //TODO LINUX goes here

    private static final Map<Platform, PlatformCommandProvider> LOOKUP_MAP = new EnumMap<>(Platform.class);

    static {
        for (final PlatformCommandProvider p : values()) {
            LOOKUP_MAP.put(p.platform, p);
        }
    }

    private final Platform platform;

    private PlatformCommandProvider(final Platform thePlatform) {
        platform = thePlatform;
    }

    public static PlatformCommandProvider lookup(final Platform aPlatform) {
        return LOOKUP_MAP.get(aPlatform);
    }

    public abstract ServiceCommandBuilder getServiceCommandBuilderFor(final JvmControlOperation anOperation);

    public abstract ServiceCommandBuilder getServiceCommandBuilderFor(final WebServerControlOperation anOperation);

    public abstract ServiceCommandBuilder getGenericServiceCommandBuilder();

}
