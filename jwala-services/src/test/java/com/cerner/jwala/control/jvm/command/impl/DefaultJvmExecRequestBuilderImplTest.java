package com.cerner.jwala.control.jvm.command.impl;

import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.DefaultExecCommandBuilderImpl;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
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

    private JpaJvm jvm;
    private DefaultExecCommandBuilderImpl impl;
    private String jvmName;
    String originalPRP = null;

    @After
    public void tearDown() {
        if (originalPRP != null) {
            System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, originalPRP);
        }
    }

    @Before
    public void setup() {
        originalPRP = System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "src/test/resources");
        try {
            ApplicationProperties.getInstance();
        } catch (ApplicationException e) {
            LOGGER.trace("Attempting to load properties without project in path", e);
            System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
            ApplicationProperties.getInstance();
        }

        impl = new DefaultExecCommandBuilderImpl();
        jvm = mock(JpaJvm.class);
        jvmName = "theJvmName";

        when(jvm.getName()).thenReturn(jvmName);
    }

    @Test
    public void testStart() throws Exception {

        final JvmControlOperation operation = JvmControlOperation.START;

        impl.setEntityName(jvm.getName());
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build(new WindowsJvmPlatformCommandProvider());
        final ShellCommand expectedCommand = new ShellCommand("`/usr/bin/cygpath d:/jwala/app/instances/theJvmName/bin/start-service.sh`",
                "\"" + jvmName + "\"", "20");
        assertEquals(expectedCommand,
                actualCommand);
    }

    @Test
    public void testStop() throws Exception {

        final JvmControlOperation operation = JvmControlOperation.STOP;

        impl.setEntityName(jvm.getName());
        impl.setOperation(operation);

        final ExecCommand actualCommand = impl.build(new WindowsJvmPlatformCommandProvider());

        assertTrue(actualCommand.getCommandFragments().size() > 0);
    }
}
