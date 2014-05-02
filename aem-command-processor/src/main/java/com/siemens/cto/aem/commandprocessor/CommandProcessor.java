package com.siemens.cto.aem.commandprocessor;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

import com.siemens.cto.aem.commandprocessor.domain.ExecutionReturnCode;
import com.siemens.cto.aem.commandprocessor.domain.NotYetReturnedException;

public interface CommandProcessor extends Closeable {

    InputStream getCommandOutput();

    InputStream getErrorOutput();

    OutputStream getCommandInput();

    ExecutionReturnCode getExecutionReturnCode() throws NotYetReturnedException;
}
