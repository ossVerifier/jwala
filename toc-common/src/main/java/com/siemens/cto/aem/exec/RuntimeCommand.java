package com.siemens.cto.aem.exec;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class RuntimeCommand {
    private String command;
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeCommand.class);
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    public RuntimeCommand(String commandToExecute) {
        command = commandToExecute;
    }

    public CommandOutput execute() {
        Runtime rt = Runtime.getRuntime();
        LOGGER.info("Running command: {}", command);
        try {
            Process proc = rt.exec(command);
            final StringBuilder inputBuffer = new StringBuilder();
            InputStream inputStream = proc.getInputStream();
            final StringBuilder errorBuffer = new StringBuilder();
            InputStream errorStream = proc.getErrorStream();

            new ProcessLogger(inputBuffer, inputStream, "SYSTEM");
            new ProcessLogger(errorBuffer, errorStream, "ERROR");

            proc.waitFor();

            return new CommandOutput(new ExecReturnCode(proc.exitValue()),
                    inputBuffer.toString(),
                    errorBuffer.toString());
        } catch (IOException e) {
            LOGGER.error("Failed running command IOException :: ERROR: {}", e.getMessage());
            throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, "Failed running command IOException", e);
        } catch (InterruptedException e) {
            LOGGER.error("Failed running command InterruptedException:: ERROR: {}", e.getMessage());
            throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, "Failed running command InterruptedException", e);
        }
    }

    @Override
    public String toString() {
        return command;
    }

    class ProcessLogger extends Thread {

        private final InputStream stream;
        private final StringBuilder captureBuffer;
        private final String processType;

        public ProcessLogger(final StringBuilder captureBuffer,
                             final InputStream stream,
                             final String processType) {
            this.stream = stream;
            this.captureBuffer = captureBuffer;
            this.processType = processType;
            this.start();
        }

        public void run() {
            try {
                int nextChar = this.stream.read();
                StringBuilder lineBuff = new StringBuilder();
                while (nextChar != -1) {
                    lineBuff.append((char) nextChar);
                    if (lineBuff.toString().endsWith(LINE_SEPARATOR)) {
                        final String line = lineBuff.toString();
                        if (!line.equals(LINE_SEPARATOR)) {
                            LOGGER.info(this.processType + ": " + line.substring(0, line.length() - LINE_SEPARATOR.length()));
                        }
                        lineBuff = new StringBuilder();
                    }
                    this.captureBuffer.append((char) nextChar);
                    nextChar = this.stream.read();
                }
                if(!"".equals(lineBuff.toString().trim())){
                    LOGGER.info(lineBuff.toString());
                }

            } catch (final IOException ioe) {
                LOGGER.info("Exception while executing RuntimeCommand", ioe);
            }
        }
    }
}
