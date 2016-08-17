package com.cerner.jwala.tomcat.plugin.jgroups;

import org.jgroups.Message;
import org.jgroups.stack.IpAddress;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link JGroupsMessageBuilderTest}
 *
 * Created by JC043760 on 8/16/2016
 */
public class JGroupsMessageBuilderTest {

    @Test
    public void testBuildMsg() throws Exception {
        final Message msg = new JGroupsMessageBuilder().setInstanceId("1")
                .setAsOf(ISODateTimeFormat.dateTime().print(DateTime.now()))
                .setState("JVM_STOPPED")
                .setSrcAddress(new IpAddress("localhost"))
                .setDestAddress(new IpAddress("localhost"))
                .build();
        assertNotNull(msg);
    }
}
