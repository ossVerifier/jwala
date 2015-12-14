package com.siemens.cto.aem.control.jvm;

import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;

public interface JvmCommandExecutor {

    CommandOutput controlJvm(final ControlJvmRequest aCommand,
                             final JpaJvm aJvm) throws CommandFailureException;

}
