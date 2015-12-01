package com.siemens.cto.aem.commandprocessor;

import com.siemens.cto.aem.exec.ExecReturnCode;
import com.siemens.cto.aem.exception.NotYetReturnedException;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public interface CommandProcessor extends Closeable {

    InputStream getCommandOutput();

    InputStream getErrorOutput();

    OutputStream getCommandInput();

    ExecReturnCode getExecutionReturnCode() throws NotYetReturnedException;
}
