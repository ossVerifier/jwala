package com.siemens.cto.aem.commandprocessor;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;

public interface CommandProcessor extends Closeable {

    InputStream getCommandOutput();

    InputStream getErrorOutput();

    OutputStream getCommandInput();

    ExecReturnCode getExecutionReturnCode() throws NotYetReturnedException;
}
