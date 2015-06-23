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
    @Test
    public void testCleanStandardOutput(){
        ExecData testObject = new ExecData(new ExecReturnCode(0), STANDARD_OUTPUT_WITH_SPECIAL_CHARS,"");
        testObject.cleanStandardOutput();
        assertEquals(STANDARD_OUTPUT_WITH_SPECIAL_CHARS_REMOVED, testObject.getStandardOutput());
    }
}
