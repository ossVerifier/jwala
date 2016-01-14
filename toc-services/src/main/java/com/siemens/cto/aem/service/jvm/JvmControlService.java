package com.siemens.cto.aem.service.jvm;

import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface JvmControlService {

    CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser);

    CommandOutput secureCopyFile(ControlJvmRequest secureCopyRequest, String sourcePath, String destPath) throws CommandFailureException;

    CommandOutput secureCopyFileWithBackup(ControlJvmRequest secureCopyRequest, String sourcePath, String destPath) throws CommandFailureException;
}
