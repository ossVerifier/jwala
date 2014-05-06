package com.siemens.cto.aem.control.jvm.command;

import com.siemens.cto.aem.domain.model.exec.ExecCommand;

public interface ServiceCommandBuilder {

    ExecCommand buildCommandForService(final String aServiceName);
}
