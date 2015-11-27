package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.command.jvm.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.user.User;

public interface JvmControlService {

    CommandOutput controlJvm(final ControlJvmCommand aCommand, final User aUser);
}
