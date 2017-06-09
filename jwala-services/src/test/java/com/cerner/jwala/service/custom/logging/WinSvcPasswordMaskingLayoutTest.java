package com.cerner.jwala.service.custom.logging;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link WinSvcPasswordMaskingLayout}
 * Created by Jedd Cuison on 6/7/2017
 */
public class WinSvcPasswordMaskingLayoutTest {

    private static final String LOG_MSG_WITH_PASSWORD_1 = "Executing command \"~/.jwala/CTO-N9SF-LTST-HEALTH-CHECK-4.0-" +
            "USMLVV1CDS0049-2/install-service.sh CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CDS0049-2 D:/ctp/app/instances " +
            "apache-tomcat-7.0.55 \"jedi\" the-password \"";
    private static final String LOG_MSG_WITH_PASSWORD_2 = "D:\\cygwin64\\home\\N9SFGLabTomcatAdmin>set svc_password=the-password ";
    private static final String UNMASKED_PASSWORD_SNIPPET_1 = "\"jedi\" the-password \"";
    private static final String UNMASKED_PASSWORD_SNIPPET_2 = "svc_password=the-password ";
    private static ByteArrayOutputStream consoleOut;
    private static PrintStream printStream;
    private static final PrintStream stdout = System.out;
    private static Logger logger;

    @BeforeClass
    public static void init() {
        consoleOut = new ByteArrayOutputStream();
        printStream = new PrintStream(consoleOut);
        System.setOut(printStream);
        logger = LoggerFactory.getLogger(WinSvcPasswordMaskingLayout.class);
    }

    @AfterClass
    public static void destroy() throws IOException {
        System.setOut(stdout);
        printStream.close();
        consoleOut.close();
    }

    @Test
    public void testMaskSvcPassword() {
        logger.debug(LOG_MSG_WITH_PASSWORD_1);
        assertTrue(consoleOut.toString().contains("\"jedi\" ******** "));
        logger.debug(LOG_MSG_WITH_PASSWORD_2);
        assertTrue(consoleOut.toString().contains("svc_password=********"));
    }

    @Test
    public void testLogLevelNotDebug() {
        logger.info(LOG_MSG_WITH_PASSWORD_1);
        assertTrue(consoleOut.toString().contains(UNMASKED_PASSWORD_SNIPPET_1));
        logger.error(LOG_MSG_WITH_PASSWORD_2);
        assertTrue(consoleOut.toString().contains(UNMASKED_PASSWORD_SNIPPET_2));
    }

    /**
     * This method is not in log4j.xml therefore masking will not be applied
     */
    @Test
    public void testMethodExcludedFromMasking() {
        logger.debug(LOG_MSG_WITH_PASSWORD_1);
        assertTrue(consoleOut.toString().contains(UNMASKED_PASSWORD_SNIPPET_1));
        logger.debug(LOG_MSG_WITH_PASSWORD_2);
        assertTrue(consoleOut.toString().contains(UNMASKED_PASSWORD_SNIPPET_2));
    }

    /**
     *  The class used in this test is not in log4j.xml therefore masking will not be applied
     */
    @Test
    public void testClassExcludedFromMasking() {
        final SomeClassThatLogs someClassThatLogs = new SomeClassThatLogs(logger);
        someClassThatLogs.writeLog1();
        assertTrue(consoleOut.toString().contains(UNMASKED_PASSWORD_SNIPPET_1));
        someClassThatLogs.writeLog2();
        assertTrue(consoleOut.toString().contains(UNMASKED_PASSWORD_SNIPPET_2));
    }

    /**
     * A class used to test class "svc_password masking exclusion"
     */
    private static class SomeClassThatLogs {

        private final Logger logger;

        public SomeClassThatLogs(final Logger logger) {
            this.logger = logger;
        }

        void writeLog1() {
            logger.debug(LOG_MSG_WITH_PASSWORD_1);
        }

        void writeLog2() {
            logger.debug(LOG_MSG_WITH_PASSWORD_2);
        }
    }
}