package com.siemens.cto.aem.control.jvm.command.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;

public class DefaultJvmExecCommandBuilderImplTest {

    private Jvm jvm;
    private DefaultJvmExecCommandBuilderImpl impl;
    private String jvmName;

    @Before
    public void setup() {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "src/test/resources");
        impl = new DefaultJvmExecCommandBuilderImpl();
        jvm = mock(Jvm.class);
        jvmName = "theJvmName";

        when(jvm.getJvmName()).thenReturn(jvmName);
    }

    @Test
    public void testStart() throws Exception {

        final JvmControlOperation operation = JvmControlOperation.START;

        impl.setJvm(jvm);
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build();
        final ExecCommand expectedCommand = new ExecCommand("net",
                                                            "start",
                                                            "\"" + jvmName + "\"");
        assertEquals(expectedCommand,
                     actualCommand);
    }

    @Test
    public void testStop() throws Exception {

        final JvmControlOperation operation = JvmControlOperation.STOP;

        impl.setJvm(jvm);
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build();
        
        assertTrue(actualCommand.getCommandFragments().size() > 0);
    }
}
