package com.siemens.cto.aem.control.command;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.common.exec.RuntimeCommand;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuntimeRequestBuilderTest {

    @Test
    public void testBuild() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./toc-control/src/test/resources");
        RuntimeCommandBuilder rtCommandBuilder = new RuntimeCommandBuilder();
        rtCommandBuilder.setOperation(AemControl.Properties.SCP_SCRIPT_NAME);
        rtCommandBuilder.addParameter("/test/param");
        rtCommandBuilder.addCygwinPathParameter("/wrapped/param");
        RuntimeCommand result = rtCommandBuilder.build();
        String cmdStr = result.toString();
        assertTrue(cmdStr.contains("/test/param"));
        assertTrue(cmdStr.contains("/wrapped/param"));
        assertTrue(cmdStr.contains("cygpath"));
        assertTrue(cmdStr.contains("secure-copy.sh"));

        rtCommandBuilder.reset();
        result = rtCommandBuilder.build();
        cmdStr = result.toString();
        assertFalse(cmdStr.contains("/test/param"));
        assertFalse(cmdStr.contains("/wrapped/param"));
        assertFalse(cmdStr.contains("secure-copy.sh"));
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }
}
