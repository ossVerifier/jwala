package com.siemens.cto.aem.commandprocessor;

import java.io.InputStream;
import java.io.OutputStream;

public interface CommandProcessor extends AutoCloseable {

    InputStream getCommandOutput();

    InputStream getErrorOutput();

    OutputStream getCommandInput();
}
