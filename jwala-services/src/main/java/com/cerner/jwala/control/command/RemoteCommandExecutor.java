package com.cerner.jwala.control.command;

import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;

public interface RemoteCommandExecutor<T> {
    CommandOutput executeRemoteCommand(final String entityName, final String entityHost, final T remoteOperation, final PlatformCommandProvider provider, String... params) throws CommandFailureException;
}
