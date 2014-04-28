package com.siemens.cto.aem.commandprocessor.impl.cli;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;
import com.siemens.cto.aem.commandprocessor.SimpleCommandProcessor;
import com.siemens.cto.aem.commandprocessor.impl.SimpleCommandProcessorImpl;
import com.siemens.cto.aem.commandprocessor.impl.WindowsTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalRuntimeCommandProcessorImplTest extends WindowsTest {

    @Test
    public void testSendSomeInput() throws Exception {

        final LocalRuntimeCommandProcessorImpl impl = new LocalRuntimeCommandProcessorImpl(new ExecCommand("cmd", "/c", "date"));
        final OutputStream commandInput = impl.getCommandInput();
        commandInput.write("\n".getBytes(StandardCharsets.UTF_8));
        commandInput.flush();
        commandInput.close();

        final SimpleCommandProcessor processor = new SimpleCommandProcessorImpl(impl);
        final String commandOutput = processor.getCommandOutput();
        assertTrue(commandOutput.contains("Enter the new date"));
    }

    @Test
    public void testCloseThatShouldDestroy() throws Exception {

        final LocalRuntimeCommandProcessorImpl impl = new LocalRuntimeCommandProcessorImpl(new ExecCommand("cmd", "/c", "date"));
        impl.close();
        assertTrue(impl.wasClosed());
        assertTrue(impl.wasTerminatedAbnormally());
    }

    @Test
    public void testCloseThatShouldNotDestroy() throws Exception {

        final LocalRuntimeCommandProcessorImpl impl = new LocalRuntimeCommandProcessorImpl(new ExecCommand("cmd", "/c", "date", "/t"));
        final SimpleCommandProcessor processor = new SimpleCommandProcessorImpl(impl);  //This is necessary to consume the output, otherwise the code executes before the command completes and the test fails
        impl.close();
        assertTrue(impl.wasClosed());
        assertFalse(impl.wasTerminatedAbnormally());
    }

}
