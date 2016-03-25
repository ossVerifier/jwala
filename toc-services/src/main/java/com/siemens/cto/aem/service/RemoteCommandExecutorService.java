package com.siemens.cto.aem.service;

import com.siemens.cto.aem.common.exec.RemoteExecCommand;

/**
 * Defines a contract for executing remote commands.
 *
 * Created by JC043760 on 3/25/2016.
 */
public interface RemoteCommandExecutorService {

    RemoteCommandReturnInfo executeCommand(RemoteExecCommand remoteExecCommand);

}
