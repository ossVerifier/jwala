package com.siemens.cto.aem.commandprocessor.impl;

import org.junit.Test;

import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;
import com.siemens.cto.aem.commandprocessor.impl.cli.LocalRuntimeCommandProcessorImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleCommandProcessorImplTest extends WindowsTest {

    @Test
    public void testSimpleExecute() throws Exception {

        final SimpleCommandProcessorImpl impl = new SimpleCommandProcessorImpl(new LocalRuntimeCommandProcessorImpl(new ExecCommand("cmd", "/c", "type", "c:\\temp\\test.txt")));

        final String output = impl.getCommandOutput();
        final String errorOutput = impl.getErrorOutput();
        assertTrue(output.contains("This is only a test"));
        assertEquals("",
                     errorOutput);
    }

    @Test
    public void testSimpleExecuteWithErrorOutput() throws Exception {

        final SimpleCommandProcessorImpl impl = new SimpleCommandProcessorImpl(new LocalRuntimeCommandProcessorImpl(new ExecCommand("cmd", "/c", "type", "c:\\temp\\IShouldNotExist.abcdefg")));
        final String output = impl.getCommandOutput();
        final String errorOutput = impl.getErrorOutput();

        assertEquals("",
                     output);
        assertTrue(errorOutput.contains("The system cannot find the file specified"));
    }
}
