package com.siemens.cto.aem.common.exec;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RuntimeRequestTest {

    @Test
    public void testExecute() {
        RuntimeCommand rtCommand = new RuntimeCommand("C:\\WINDOWS\\system32\\cmd.exe /c echo Hello World");
        CommandOutput result = rtCommand.execute();
        assertTrue(result.getReturnCode().wasSuccessful());
    }
}
