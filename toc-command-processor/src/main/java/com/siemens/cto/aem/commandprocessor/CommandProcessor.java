package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;

import java.io.Closeable;

public interface CommandProcessor extends Closeable {

    ExecReturnCode getExecutionReturnCode();

    void processCommand() throws RemoteCommandFailureException;

    String getCommandOutputStr();

    String getErrorOutputStr();

}
