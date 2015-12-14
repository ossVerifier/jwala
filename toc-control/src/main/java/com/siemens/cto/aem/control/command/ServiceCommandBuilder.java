package com.siemens.cto.aem.control.command;

import com.siemens.cto.aem.common.exec.ExecCommand;

public interface ServiceCommandBuilder {

    ExecCommand buildCommandForService(final String aServiceName, final String...aParams);
}
