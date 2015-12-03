package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.domain.model.user.User;

public interface JvmControlService {

    CommandOutput controlJvm(final ControlJvmRequest aCommand, final User aUser);
}
