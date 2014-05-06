package com.siemens.cto.aem.commandprocessor.impl.cli;

import org.junit.Test;

import com.siemens.cto.aem.commandprocessor.impl.WindowsTest;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public class LocalRuntimeCommandProcessorBuilderTest extends WindowsTest {

    @Test(expected = CommandFailureException.class)
    public void testBadCommand() throws Exception {
        final LocalRuntimeCommandProcessorBuilder builder = new LocalRuntimeCommandProcessorBuilder();
        builder.setCommand(new ExecCommand("this.command.should.not.exist"));
        builder.build();
    }
}
