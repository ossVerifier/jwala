package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;

import java.io.Closeable;

public interface CommandProcessor extends Closeable {

    ExecReturnCode getExecutionReturnCode() throws NotYetReturnedException;

    void processCommand() throws RemoteCommandFailureException;

    String getCommandOutputStr();

    String getErrorOutputStr();

}
