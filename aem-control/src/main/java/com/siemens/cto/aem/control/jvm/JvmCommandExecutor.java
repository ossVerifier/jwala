package com.siemens.cto.aem.control.jvm;

import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface JvmCommandExecutor {

    CommandOutput controlJvm(final ControlJvmRequest aCommand,
                             final Jvm aJvm) throws CommandFailureException;
}
