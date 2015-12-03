package com.siemens.cto.aem.control.jvm.command.impl;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.common.ApplicationException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.exec.ExecCommand;
import com.siemens.cto.aem.exec.ShellCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultJvmExecRequestBuilderImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultJvmExecRequestBuilderImplTest.class);

    private Jvm jvm;
    private DefaultJvmExecCommandBuilderImpl impl;
    private String jvmName;
    String originalPRP = null;

    @After
    public void tearDown() {
        if (originalPRP != null) {
            System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, originalPRP);
        }
    }

    @Before
    public void setup() {
        originalPRP = System.getProperty(AemConstants.PROPERTIES_ROOT_PATH);
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "toc-control/src/test/resources");
        try {
            ApplicationProperties.getInstance();
        } catch (ApplicationException e) {
            LOGGER.trace("Attempting to load properties without project in path", e);
            System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "src/test/resources");
            ApplicationProperties.getInstance();
        }

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
        final ShellCommand expectedCommand = new ShellCommand("`/usr/bin/cygpath /cygdrive/d/stp/siemens/lib/scripts/start-service.sh`",
                "\"" + jvmName + "\"", "20");
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
