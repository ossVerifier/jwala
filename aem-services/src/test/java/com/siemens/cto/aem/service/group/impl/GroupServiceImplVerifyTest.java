package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationWorker;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class GroupServiceImplVerifyTest extends VerificationBehaviorSupport {

    private GroupServiceImpl impl;
    private GroupPersistenceService groupPersistenceService;
    private GroupStateService.API groupStateService;
    private StateNotificationWorker stateNotificationWorker;
    private WebServerService webServerService;
    private User user;

    @Before
    public void setUp() {
        groupPersistenceService = mock(GroupPersistenceService.class);
        stateNotificationWorker = mock(StateNotificationWorker.class);
        groupStateService = mock(GroupStateService.API.class);
        stateNotificationWorker = mock(StateNotificationWorker.class);
        webServerService = mock(WebServerService.class);

        impl = new GroupServiceImpl(groupPersistenceService,
                                    webServerService,
                                    groupStateService,
                                    stateNotificationWorker);
        user = new User("unused");
    }

    @Test
    public void testCreateGroup() {

        final CreateGroupCommand command = mock(CreateGroupCommand.class);

        impl.createGroup(command,
                         user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).createGroup(matchCommandInEvent(command));
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.getGroup(id);

        verify(groupPersistenceService, times(1)).getGroup(eq(id));
    }

    @Test
    public void testGetGroups() {

        impl.getGroups();

        verify(groupPersistenceService, times(1)).getGroups();
    }

    @Test
    public void testFindGroups() {

        final String fragment = "unused";

        impl.findGroups(fragment);

        verify(groupPersistenceService, times(1)).findGroups(eq(fragment));
    }

    @Test(expected = BadRequestException.class)
    public void testFindGroupsWithBadName() {

        final String badFragment = "";

        impl.findGroups(badFragment);
    }

    @Test
    public void testUpdateGroup() throws InterruptedException {
        final UpdateGroupCommand command = mock(UpdateGroupCommand.class);
        impl.updateGroup(command, user);

        verify(command).validateCommand();
        verify(groupPersistenceService).updateGroup(matchCommandInEvent(command));
        verify(stateNotificationWorker).refreshState(eq(groupStateService), any(Group.class));
    }

    @Test
    public void testRemoveGroup() {

        final Identifier<Group> id = new Identifier<>(-123456L);

        impl.removeGroup(id);

        verify(groupPersistenceService, times(1)).removeGroup(eq(id));
    }

    @Test
    public void testAddJvmToGroup() {

        final AddJvmToGroupCommand command = mock(AddJvmToGroupCommand.class);

        impl.addJvmToGroup(command,
                           user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).addJvmToGroup(matchCommandInEvent(command));
        verify(stateNotificationWorker).refreshState(eq(groupStateService), any(Group.class));
    }

    @Test
    public void testAddJvmsToGroup() {

        final AddJvmsToGroupCommand command = mock(AddJvmsToGroupCommand.class);

        final Set<AddJvmToGroupCommand> addCommands = createMockedAddCommands(5);
        when(command.toCommands()).thenReturn(addCommands);

        impl.addJvmsToGroup(command,
                            user);

        verify(command, times(1)).validateCommand();
        for (final AddJvmToGroupCommand addCommand : addCommands) {
            verify(addCommand, times(1)).validateCommand();
            verify(groupPersistenceService, times(1)).addJvmToGroup(matchCommandInEvent(addCommand));
        }
    }

    @Test
    public void testRemoveJvmFromGroup() {

        final RemoveJvmFromGroupCommand command = mock(RemoveJvmFromGroupCommand.class);

        impl.removeJvmFromGroup(command,
                                user);

        verify(command, times(1)).validateCommand();
        verify(groupPersistenceService, times(1)).removeJvmFromGroup(matchCommandInEvent(command));
        verify(stateNotificationWorker).refreshState(eq(groupStateService), any(Group.class));
    }

    @Test
    public void testGetOtherGroupingDetailsOfJvms() {
        final Set<LiteGroup> groupSet = new HashSet<>();
        groupSet.add(new LiteGroup(new Identifier<Group>("1"), "Group1"));
        groupSet.add(new LiteGroup(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new LiteGroup(new Identifier<Group>("3"), "Group3"));

        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(new Jvm(new Identifier<Jvm>("1"), "Jvm1", groupSet));

        final Group group = new Group(new Identifier<Group>("1"), "Group1" , jvmSet);

        when(groupPersistenceService.getGroup(any(Identifier.class), eq(false))).thenReturn(group);

        final List<Jvm> otherGroupingDetailsOfJvm = impl.getOtherGroupingDetailsOfJvms(new Identifier<Group>("1"));

        assertTrue(otherGroupingDetailsOfJvm.size() == 1);
        assertEquals(otherGroupingDetailsOfJvm.get(0).getGroups().size(), 2);

        String groupNames = "";
        for (LiteGroup grp: otherGroupingDetailsOfJvm.get(0).getGroups()) {
            groupNames += grp.getName();
        }

        assertTrue("Group3Group2".equalsIgnoreCase(groupNames) || "Group2Group3".equalsIgnoreCase(groupNames));
    }

    @Test
    public void testGetOtherGroupingDetailsOfWebServers() {
        final List<Group> groupSet = new ArrayList<>();
        groupSet.add(new Group(new Identifier<Group>("2"), "Group2"));
        groupSet.add(new Group(new Identifier<Group>("3"), "Group3"));

        final Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(new WebServer(new Identifier<WebServer>("1"),
                                       groupSet,
                                       "WebServer1",
                                       null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null));

        groupSet.add(new Group(new Identifier<Group>("1"), "Group1", new HashSet<Jvm>(), webServerSet, null, null));

        when(groupPersistenceService.getGroup(any(Identifier.class), eq(true))).thenReturn(groupSet.get(2));

        final List<WebServer> otherGroupingDetailsOfWebServer =
                impl.getOtherGroupingDetailsOfWebServers(new Identifier<Group>("1"));
        assertTrue(otherGroupingDetailsOfWebServer.size() == 1);

        String groupNames = "";
        for (Group grp: otherGroupingDetailsOfWebServer.get(0).getGroups()) {
            groupNames += grp.getName();
        }

        assertTrue("Group3Group2".equalsIgnoreCase(groupNames) || "Group2Group3".equalsIgnoreCase(groupNames));
    }

}
