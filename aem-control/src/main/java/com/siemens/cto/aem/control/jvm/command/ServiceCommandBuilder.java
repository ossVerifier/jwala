package com.siemens.cto.aem.control.jvm.command;

import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;

public interface ServiceCommandBuilder {

    ExecCommand buildCommandForService(final String aServiceName);
}
