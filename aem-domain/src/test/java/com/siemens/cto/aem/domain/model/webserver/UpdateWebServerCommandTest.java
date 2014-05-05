package com.siemens.cto.aem.domain.model.webserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class UpdateWebServerCommandTest {
    private static final String HOST = "host";

    private static final String NAME = "name";

    private static final Integer port = Integer.valueOf(10000);

    private static final Long id = Long.valueOf(1);
    private static final Long idOne = Long.valueOf(1);
    private static final Long idTwo = Long.valueOf(2);

    private static final Identifier<WebServer> wsId = new Identifier<WebServer>(id);
    private static final Identifier<WebServer> wsIdOne = new Identifier<WebServer>(idOne);
    private static final Identifier<WebServer> wsIdTwo = new Identifier<WebServer>(idTwo);

    List<Identifier<Group>> groupIds = new ArrayList<Identifier<Group>>();

    final Collection<Identifier<Group>> groupIdsOne = new ArrayList<Identifier<Group>>();
    final Collection<Identifier<Group>> groupIdsTwo = new ArrayList<Identifier<Group>>();
    final Collection<Identifier<Group>> groupIdsFour = new ArrayList<Identifier<Group>>();

    final UpdateWebServerCommand ws = new UpdateWebServerCommand(wsId, groupIds, NAME, HOST, port);
    final UpdateWebServerCommand wsOne = new UpdateWebServerCommand(wsIdOne, groupIds, NAME, HOST, port);
    final UpdateWebServerCommand wsTwo = new UpdateWebServerCommand(wsIdTwo, groupIdsOne, NAME, HOST, port);
    final UpdateWebServerCommand wsThree = new UpdateWebServerCommand(null, null, NAME, HOST, port);
    final UpdateWebServerCommand wsFour = new UpdateWebServerCommand(null, groupIdsFour, NAME, HOST, port);
    final UpdateWebServerCommand wsFive = new UpdateWebServerCommand(wsIdOne, groupIdsFour, NAME, null, port);
    final UpdateWebServerCommand wsSix = new UpdateWebServerCommand(wsIdOne, null, NAME, HOST, port);
    final UpdateWebServerCommand wsSeven = new UpdateWebServerCommand(wsIdOne, null, NAME, HOST, port);
    final UpdateWebServerCommand wsEight = new UpdateWebServerCommand(wsIdOne, groupIdsFour, NAME, HOST, port);
    final UpdateWebServerCommand wsNine = new UpdateWebServerCommand(wsIdOne, groupIdsFour, NAME, HOST, port);
    final UpdateWebServerCommand wsTen = new UpdateWebServerCommand(wsId, groupIdsFour, NAME, null, port);
    final UpdateWebServerCommand wsEleven = new UpdateWebServerCommand(wsId, groupIdsFour, NAME, null, null);
    final UpdateWebServerCommand wsTwelve = new UpdateWebServerCommand(wsId, groupIdsFour, NAME, "otherHost", null);
    final UpdateWebServerCommand wsThirteen = new UpdateWebServerCommand(wsId, groupIdsFour, null, HOST, port);
    final UpdateWebServerCommand wsFourteen = new UpdateWebServerCommand(wsId, groupIdsFour, null, HOST, port);
    final UpdateWebServerCommand wsFifteen = new UpdateWebServerCommand(wsIdOne, groupIdsFour, "otherName", HOST, port);
    final UpdateWebServerCommand wsSixteen = new UpdateWebServerCommand(wsIdOne, groupIdsFour, NAME, HOST, null);
    final UpdateWebServerCommand wsSeventeen = new UpdateWebServerCommand(wsIdOne, groupIdsFour, NAME, HOST, null);
    final UpdateWebServerCommand wsEighteen = new UpdateWebServerCommand(wsIdOne, groupIdsFour, NAME, HOST, port);
    final UpdateWebServerCommand wsNulls = new UpdateWebServerCommand(null, null, null, null, null);

    @Test
    public void testHashCode() {
        assertEquals(wsOne.hashCode(), wsOne.hashCode());
        assertEquals(31 * 31 * 31 * 31 * 31, wsNulls.hashCode());
    }

    @Test
    public void testGetId() {
        assertEquals(wsId, ws.getId());
    }

    @Test
    public void testGetNewName() {
        assertEquals(NAME, ws.getNewName());
    }

    @Test
    public void testGetNewHost() {
        assertEquals(HOST, ws.getNewHost());
    }

    @Test
    public void testGetNewPort() {
        assertEquals(port, ws.getNewPort());
    }

    @Test
    public void testGetNewGroups() {
        assertEquals(0, ws.getNewGroupIds().size());
    }

    @Test
    public void testValidateCommand() {
        ws.validateCommand();
    }

    @Test
    public void testEqualsObject() {
        groupIdsFour.add(null);

        assertTrue(wsOne.equals(wsOne));
        assertFalse(wsOne.equals(null));
        assertFalse(wsOne.equals(""));
        assertFalse(wsThree.equals(wsOne));
        assertFalse(wsThree.equals(wsFour));
        assertFalse(wsOne.equals(wsTwo));
        assertFalse(wsOne.equals(wsFive));
        assertTrue(wsSix.equals(wsSeven));
        assertFalse(wsOne.equals(wsEight));
        assertTrue(wsEight.equals(wsNine));
        assertFalse(wsTen.equals(wsNine));
        assertFalse(wsTen.equals(wsEleven));
        assertFalse(wsNine.equals(wsTwelve));
        assertFalse(wsThirteen.equals(wsEight));
        assertTrue(wsThirteen.equals(wsFourteen));
        assertFalse(wsNine.equals(wsFifteen));
        assertFalse(wsSixteen.equals(wsFifteen));
        assertTrue(wsSixteen.equals(wsSeventeen));
        assertFalse(wsSeventeen.equals(wsEighteen));
    }

    @Test
    public void testToString() {
        assertEquals(
                "UpdateWebServerCommand {id=Identifier{id=1}, groupIds=[], newHost=host, newName=name, newPort=10000}",
                ws.toString());
    }
}
