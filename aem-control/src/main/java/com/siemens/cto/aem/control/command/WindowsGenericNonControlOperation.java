package com.siemens.cto.aem.control.command;

import com.siemens.cto.aem.domain.model.exec.ExecCommand;

public enum WindowsGenericNonControlOperation implements ServiceCommandBuilder {

    QUERY_SERVICE_EXISTENCE {
        @Override
        public ExecCommand buildCommandForService(final String aServiceName,
                                                  final String... aParams) {
            return new ExecCommand("sc", "query", quotedServiceName(aServiceName));
        }
    };

    private static String quotedServiceName(final String aServiceName) {
        return "\"" + aServiceName + "\"";
    }
}
