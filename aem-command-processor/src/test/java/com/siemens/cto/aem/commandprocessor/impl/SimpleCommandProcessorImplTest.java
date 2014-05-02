package com.siemens.cto.aem.commandprocessor.impl;

import org.junit.Test;

import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;
import com.siemens.cto.aem.commandprocessor.impl.cli.LocalRuntimeCommandProcessorImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleCommandProcessorImplTest extends WindowsTest {

    @Test
    public void testSimpleExecute() throws Exception {

        try (final LocalRuntimeCommandProcessorImpl command = new LocalRuntimeCommandProcessorImpl(new ExecCommand("cmd", "/c", "ipconfig"))) {
            final SimpleCommandProcessorImpl impl = new SimpleCommandProcessorImpl(command);

            final String output = impl.getCommandOutput();
            final String errorOutput = impl.getErrorOutput();
            assertTrue(output.contains("Windows IP Configuration"));
            assertEquals("",
                         errorOutput);
        }
    }

    @Test
    public void testSimpleExecuteWithErrorOutput() throws Exception {

        try (final LocalRuntimeCommandProcessorImpl command = new LocalRuntimeCommandProcessorImpl(new ExecCommand("cmd", "/c", "type", "c:\\temp\\IShouldNotExist.abcdefg"))) {

            final SimpleCommandProcessorImpl impl = new SimpleCommandProcessorImpl(command);
            final String output = impl.getCommandOutput();
            final String errorOutput = impl.getErrorOutput();

            assertEquals("",
                         output);
            assertTrue(errorOutput.contains("The system cannot find the file specified"));
        }
    }
}
