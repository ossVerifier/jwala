package com.siemens.cto.aem.domain.model.webserver;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebServerTest {
    private static final String HOST = "host";

    private static final String NAME = "name";

    private static final Integer port = Integer.valueOf(10000);
    private static final Integer httpsPort = Integer.valueOf(20000);

    private static final Long id = Long.valueOf(1);
    private static final Long idOne = Long.valueOf(1);
    private static final Long idTwo = Long.valueOf(2);

    private static final Identifier<WebServer> wsId = new Identifier<WebServer>(id);
    private static final Identifier<WebServer> wsIdOne = new Identifier<WebServer>(idOne);
    private static final Identifier<WebServer> wsIdTwo = new Identifier<WebServer>(idTwo);

    final Identifier<Group> groupId = new Identifier<Group>(id);
    final Group group = new Group(groupId, NAME);
    final List<Group> groups = new ArrayList<Group>();
    final WebServer ws = new WebServer(wsId, groups, NAME, HOST, port, httpsPort);
    final WebServer wsNulls = new WebServer(null, groups, null, null, null, null);

    @Test
    public void testGetId() {
        assertEquals(wsId, ws.getId());
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, ws.getName());
    }

    @Test
    public void testGetHost() {
        assertEquals(HOST, ws.getHost());
    }

    @Test
    public void testGetPort() {
        assertEquals(port, ws.getPort());
    }

    @Test
    public void testGetGroups() {
        assertEquals(0, ws.getGroups().size());
    }

    @Test
    public void testGetGroupIds() {
        assertEquals(0, ws.getGroupIds().size());
    }

    @Test
    public void testEqualsObject() {
        final Group groupOne = new Group(groupId, NAME);
        final Group groupTwo = new Group(groupId, "nameTwo");

        final List<Group> groupsOne = new ArrayList<Group>();
        final List<Group> groupsTwo = new ArrayList<Group>();
        groupsOne.add(groupOne);
        groupsTwo.add(groupTwo);

        final WebServer wsOne = new WebServer(wsIdOne, groupsOne, NAME, HOST, port, httpsPort);
        final WebServer wsTwo = new WebServer(null, groupsTwo, NAME, HOST, port, httpsPort);
        final WebServer wsThree = new WebServer(null, groupsTwo, NAME, HOST, port, httpsPort);
        final WebServer wsFour = new WebServer(null, groupsTwo, NAME, null, port, httpsPort);
        final WebServer wsFive = new WebServer(null, groupsTwo, NAME, null, port, httpsPort);
        final WebServer wsSix = new WebServer(null, groupsTwo, NAME, null, port, httpsPort);
        final WebServer wsSeven = new WebServer(wsIdOne, groupsTwo, NAME, null, port, httpsPort);
        final WebServer wsEight = new WebServer(wsIdTwo, groupsTwo, NAME, null, port, httpsPort);
        final WebServer wsNine = new WebServer(wsIdOne, groupsTwo, NAME, null, port, httpsPort);
        final WebServer wsTen = new WebServer(wsIdOne, groupsTwo, null, null, port, httpsPort);
        final WebServer wsEleven = new WebServer(wsIdOne, groupsTwo, null, null, port, httpsPort);
        final WebServer wsTwelve = new WebServer(wsIdOne, groupsTwo, "nameTwo", null, port, httpsPort);
        final WebServer wsThirteen = new WebServer(wsIdOne, groupsTwo, "nameTwo", null, null, null);
        final WebServer wsFourteen = new WebServer(wsIdOne, groupsTwo, "nameTwo", null, null, null);
        final WebServer wsFifteen = new WebServer(wsIdOne, groupsTwo, "nameTwo", null, port, httpsPort);

        assertTrue(groupsOne.equals(groupsOne));
        assertTrue(wsOne.equals(wsOne));
        assertFalse(wsOne.equals(null));
        assertFalse(wsOne.equals(""));
        assertFalse(wsTwo.equals(wsOne));
        assertTrue(wsTwo.equals(wsThree));
        assertFalse(wsFour.equals(wsThree));
        assertTrue(wsFour.equals(wsFive));
        assertFalse(wsThree.equals(wsFour));
        assertFalse(wsSix.equals(wsSeven));
        assertFalse(wsSeven.equals(wsEight));
        assertTrue(wsSeven.equals(wsNine));
        assertFalse(wsTen.equals(wsNine));
        assertTrue(wsTen.equals(wsEleven));
        assertFalse(wsNine.equals(wsTwelve));
        assertFalse(wsThirteen.equals(wsTwelve));
        assertTrue(wsThirteen.equals(wsFourteen));
        assertFalse(wsFifteen.equals(wsFourteen));

        // assertTrue(wsThirteen.equals(wsFourteen));
        // assertFalse(wsNine.equals(wsFifteen));
        // assertFalse(wsSixteen.equals(wsFifteen));
        // assertTrue(wsSixteen.equals(wsSeventeen));
        // assertFalse(wsSeventeen.equals(wsEighteen));
    }
}
