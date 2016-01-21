package com.siemens.cto.aem.commandprocessor.impl;

/**
 * Created with IntelliJ IDEA.
 * User: LW044480
 * Date: 1/19/16
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */

import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorBuilder;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import com.siemens.cto.aem.io.FullInputStreamReader;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SimpleCommandProcessorImplTest {

    final String returnString = "return output string";

    @Test
    public void testSimpleCommandProcessorImpl() throws IOException, RemoteCommandFailureException {
        SimpleCommandProcessorImpl simpleCommandProcessor = new SimpleCommandProcessorImpl(new JschCommandProcessorBuilder().build()) {
            @Override
            protected String readAllOutput(final InputStream anInputStream) throws IOException {
                return returnString;
            }
        };
        assertTrue(returnString.equals(simpleCommandProcessor.getErrorOutput()));
        assertTrue(returnString.equals(simpleCommandProcessor.getCommandOutput()));
    }

    @Test
    public void testReadAllOutput() throws IOException {

        String testString = "test\nstring";
        final InputStream inputStream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));

        CommandProcessor commandProcessor = new CommandProcessor() {
            @Override
            public InputStream getCommandOutput() {
                return inputStream;
            }

            @Override
            public InputStream getErrorOutput() {
                return inputStream;
            }

            @Override
            public OutputStream getCommandInput() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ExecReturnCode getExecutionReturnCode() throws NotYetReturnedException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void processCommand() throws RemoteCommandFailureException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void close() throws IOException {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        SimpleCommandProcessorImpl simpleCommandProcessor = new SimpleCommandProcessorImpl(commandProcessor);
        assertTrue(simpleCommandProcessor.readAllOutput(commandProcessor.getCommandOutput()).equals(""));
        assertTrue(simpleCommandProcessor.readAllOutput(commandProcessor.getErrorOutput()).equals(""));
    }
}
