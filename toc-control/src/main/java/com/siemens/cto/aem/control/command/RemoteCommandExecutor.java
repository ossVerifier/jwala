package com.siemens.cto.aem.control.command;

import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface RemoteCommandExecutor<T> {
    CommandOutput executeRemoteCommand(final String entityName, final String entityHost, final T remoteOperation, final PlatformCommandProvider provider, String... params) throws CommandFailureException;
}
