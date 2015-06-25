package com.siemens.cto.aem.domain.model.exec;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by z0033r5b on 6/19/2015.
 */
public class ExecDataTest {
    private static final String STANDARD_OUTPUT_WITH_SPECIAL_CHARS =
            "Last login: Fri Jun 19 13:29:03 2015 from usmlvv1cto3773.usmlvv1d0a.smshsc.net\n" +
            "\u001B]0;~\u0007\n" +
            "\u001B[32mN9SFTomcatAdmin@USMLVV1CTO3773 \u001B[33m~\u001B[0m\n";
    private static final String STANDARD_OUTPUT_WITH_SPECIAL_CHARS_REMOVED =
            "Last login: Fri Jun 19 13:29:03 2015 from usmlvv1cto3773.usmlvv1d0a.smshsc.net\n" +
                    "]0;~\n" +
                    "[32mN9SFTomcatAdmin@USMLVV1CTO3773 [33m~[0m\n";
    private static final String STANDARD_OUTPUT_WITH_SHELL_INFO="`/usr/bin/cygpath d:/stp/siemens/lib/scripts/start-service.sh` \"CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CTO3773-1XD\" 120 \n" +
            "\n" +
            "exit\n" +
            "\n" +
            "Last login: Wed Jun 24 18:05:06 2015 from usmlvv1cto3773.usmlvv1d0a.smshsc.net\n" +
            "]0;~\n" +
            "[32mN9SFTomcatAdmin@USMLVV1CTO3773 [33m~[0m\n" +
            "$ `/usr/bin/cygpath d:/stp/siemens/lib/scripts/start-service.sh` \"CTO-N9SF-LTST- HEALTH-CHECK-4.0-USMLVV1CTO3773-1XD\" 120 \n" +
            "Service CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CTO3773-1XD not installed on server\n" +
            "[SC] EnumQueryServicesStatus:OpenService FAILED 1060:\n" +
            "\n" +
            "The specified service does not exist as an installed service.\n" +
            "\n" +
            "]0;~\n" +
            "[32mN9SFTomcatAdmin@USMLVV1CTO3773 [33m~[0m\n" +
            "$ \n" +
            "]0;~\n" +
            "[32mN9SFTomcatAdmin@USMLVV1CTO3773 [33m~[0m\n" +
            "$ exit\n" +
            "logout\n";
    private static final String STANDARD_OUTPUT_WITH_SHELL_INFO_REMOVED ="Service CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CTO3773-1XD not installed on server\n" +
            "[SC] EnumQueryServicesStatus:OpenService FAILED 1060:\n" +
            "\n" +
            "The specified service does not exist as an installed service.\n" +
            "\n";

    @Test
    public void testCleanStandardOutput(){
        ExecData testObject = new ExecData(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        testObject.cleanStandardOutput();
        assertEquals(STANDARD_OUTPUT_WITH_SPECIAL_CHARS_REMOVED, testObject.getStandardOutput());
    }

    @Test
    public void testExtractMessageFromStandardOutput(){
        ExecData testObject = new ExecData(new ExecReturnCode(36), STANDARD_OUTPUT_WITH_SHELL_INFO,"");
        testObject.cleanStandardOutput();
        assertEquals(STANDARD_OUTPUT_WITH_SHELL_INFO_REMOVED, testObject.extractMessageFromStandardOutput());
    }
}
