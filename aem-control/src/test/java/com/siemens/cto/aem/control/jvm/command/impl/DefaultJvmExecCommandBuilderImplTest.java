package com.siemens.cto.aem.control.jvm.command.impl;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultJvmExecCommandBuilderImplTest {

    private Jvm jvm;
    private DefaultJvmExecCommandBuilderImpl impl;
    private String jvmName;

    @Before
    public void setup() {
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
        final ExecCommand expectedCommand = new ExecCommand("net",
                                                            "stop",
                                                            "\"" + jvmName + "\"");
        assertEquals(expectedCommand,
                     actualCommand);
    }
}
