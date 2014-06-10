package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface JvmStateService {

    CurrentJvmState setCurrentJvmState(final SetJvmStateCommand aCommand,
                                       final User aUser);
}
