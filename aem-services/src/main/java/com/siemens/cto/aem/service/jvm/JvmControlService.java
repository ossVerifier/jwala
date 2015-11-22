package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.model.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface JvmControlService {

    CommandOutput controlJvm(final ControlJvmCommand aCommand, final User aUser);
}
