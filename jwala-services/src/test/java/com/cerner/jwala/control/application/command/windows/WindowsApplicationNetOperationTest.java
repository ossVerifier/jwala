package com.cerner.jwala.control.application.command.windows;

import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import static com.cerner.jwala.control.application.command.windows.WindowsApplicationNetOperation.*;

/**
 * Test for {@link WindowsApplicationNetOperation}
 * Created by Jedd Cuison on 2/14/2017.
 */
public class WindowsApplicationNetOperationTest {

    private static final String PROPERTIES_ROOT_PATH = "PROPERTIES_ROOT_PATH";
    private String resourceDir;

    public WindowsApplicationNetOperationTest() {
        resourceDir = this.getClass().getClassLoader().getResource("vars.properties").getPath();
        resourceDir = resourceDir.substring(0, resourceDir.lastIndexOf("/"));
    }

    @Before
    public void setup() {
        System.setProperty(PROPERTIES_ROOT_PATH, resourceDir);
    }

    @After
    public void tearDown() {
        System.clearProperty(PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testScp() {
        final ExecCommand execCommand =  SCP.buildCommandForService("param1", "param2", "param3");
        assertEquals("secure-copy.sh", execCommand.getCommandFragments().get(0));
        assertEquals("param2", execCommand.getCommandFragments().get(1));
        assertEquals("param3", execCommand.getCommandFragments().get(2));
    }

    @Test
    public void testBackUp() {
        final ExecCommand execCommand = BACK_UP.buildCommandForService("param1", "param2", "param3");
        assertEquals("/usr/bin/mv", execCommand.getCommandFragments().get(0));
        assertEquals("param2", execCommand.getCommandFragments().get(1));
        assertEquals("param3", execCommand.getCommandFragments().get(2));
    }

    @Test
    public void testCreateDirectory() {
        final ExecCommand execCommand = CREATE_DIRECTORY.buildCommandForService("param1", "param2");
        assertEquals("if [ ! -e \"param2\" ]; then /usr/bin/mkdir -p param2; fi;", execCommand.getCommandFragments().get(0));
    }

    @Test
    public void testChmod() {
        final ExecCommand execCommand = CHANGE_FILE_MODE.buildCommandForService("param1", "param2", "param3", "param4");
        assertEquals("/usr/bin/chmod param2 param3/param4", execCommand.getCommandFragments().get(0));
    }

    @Test
    public void testCheckFileExists() {
        final ExecCommand execCommand = CHECK_FILE_EXISTS.buildCommandForService("param1", "param2");
        assertEquals("/usr/bin/test -e param2", execCommand.getCommandFragments().get(0));
    }

    @Test
    public void testLookup() {
        assertEquals(SCP, lookup(ApplicationControlOperation.SCP));
        assertEquals(BACK_UP, lookup(ApplicationControlOperation.BACK_UP));
        assertEquals(CREATE_DIRECTORY, lookup(ApplicationControlOperation.CREATE_DIRECTORY));
        assertEquals(CHANGE_FILE_MODE, lookup(ApplicationControlOperation.CHANGE_FILE_MODE));
        assertEquals(CHECK_FILE_EXISTS, lookup(ApplicationControlOperation.CHECK_FILE_EXISTS));
    }

}
