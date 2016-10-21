package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;

import java.util.List;

public interface JvmControlService {

    CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser);

    CommandOutput controlJvmSynchronously(ControlJvmRequest controlJvmRequest, long timeout, User user) throws InterruptedException;

    CommandOutput secureCopyFile(ControlJvmRequest secureCopyRequest, String sourcePath, String destPath, String userId) throws CommandFailureException;

    CommandOutput executeCreateDirectoryCommand(Jvm jvm, String dirAbsolutePath) throws CommandFailureException;

    CommandOutput executeChangeFileModeCommand(Jvm jvm, String modifiedPermissions, String targetAbsoluteDir, String targetFile) throws CommandFailureException;

    CommandOutput executeCheckFileExistsCommand(Jvm jvm, String filename) throws CommandFailureException;

    CommandOutput executeBackUpCommand(Jvm jvm, String filename) throws CommandFailureException;

    void createJvmHistory(String jvmName, List<Group> groups, String event, EventType eventType, String user);

    void sendJvmMessage(Object object);
}
