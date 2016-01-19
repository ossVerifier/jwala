package com.siemens.cto.aem.commandprocessor.impl.jsch;

/**
 * Created with IntelliJ IDEA.
 * User: LW044480
 * Date: 1/19/16
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */

import com.siemens.cto.aem.commandprocessor.impl.SimpleCommandProcessorImpl;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import com.siemens.cto.aem.io.FullInputStreamReader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

public class SimpleCommandProcessorImplTest {

    final String returnString = "return output string";

    @Test
    public void testSimpleCommandProcessorImpl() throws IOException, RemoteCommandFailureException {
        SimpleCommandProcessorImpl simpleCommandProcessor = new SimpleCommandProcessorImpl(new JschCommandProcessorBuilder().build()){
            @Override
            protected String readAllOutput(final InputStream anInputStream) throws IOException {
                return returnString;
            }
        };
        assertTrue(returnString.equals(simpleCommandProcessor.getErrorOutput()));
        assertTrue(returnString.equals(simpleCommandProcessor.getCommandOutput()));
    }
}
