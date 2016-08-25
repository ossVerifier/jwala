package com.cerner.jwala.lifecyclelistener;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotEquals;


public class H2LifecycleServiceTest {

    @Before
    public void setup() throws IOException {
        System.setProperty("PROPERTIES_ROOT_PATH", "src/test/resources");
    }

    @Test
    public void testLoadArgumentsAndSystemPropertiesFromH2PropertiesFile() {
        System.setProperty("catalina.base","d:\\test\\my\\aa\\");
        H2LifecycleService h2LifecycleService = new H2LifecycleService();
        h2LifecycleService.loadArgumentsAndSystemPropertiesFromH2PropertiesFile();
        String[] arguments = h2LifecycleService.getArguments();
        System.out.println("----------------------------------------------------------------------");
        for (String arg : arguments) {
            System.out.println(arg);
            assertNotEquals("catalina.base", arg);
        }
    }

}
