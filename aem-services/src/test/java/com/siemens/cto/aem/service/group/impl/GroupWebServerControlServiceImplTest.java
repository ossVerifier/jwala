package com.siemens.cto.aem.service.group.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupService;

public class GroupWebServerControlServiceImplTest {
    private GroupControlPersistenceService mockPersistenceService;
    private GroupService mockGroupService;
    private CommandDispatchGateway mockCommandDispatchGateway;

    private GroupWebServerControlServiceImpl cut;
    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private Group mockGroup;
    private GroupControlHistory mockGroupControlHistory;
    private Identifier<GroupControlHistory> groupControlId;
    private User testUser = new User("testUser");
    private ControlGroupWebServerCommand aCommand;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockPersistenceService = mock(GroupControlPersistenceService.class);
        mockGroupService = mock(GroupService.class);
        mockCommandDispatchGateway = mock(CommandDispatchGateway.class);

        cut = new GroupWebServerControlServiceImpl(mockPersistenceService, mockGroupService, mockCommandDispatchGateway);

        mockGroup = mock(Group.class);
        mockGroupControlHistory = mock(GroupControlHistory.class);
        groupControlId = new Identifier<>((long) 10);
        aCommand = new ControlGroupWebServerCommand(groupId, WebServerControlOperation.START);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
        when(mockPersistenceService.addIncompleteControlHistoryEvent(any(Event.class))).thenReturn(
                mockGroupControlHistory);
        when(mockGroupControlHistory.getId()).thenReturn(groupControlId);
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWithInvalidGroup() {
        ControlGroupWebServerCommand aCommand = new ControlGroupWebServerCommand(null, WebServerControlOperation.START);
        cut.controlGroup(aCommand, testUser);
    }

    @Test
    public void testControlGroup() {
        GroupControlHistory groupControlHistory = cut.controlGroup(aCommand, testUser);
        assertNotNull(groupControlHistory);

        GroupWebServerDispatchCommand dispatchCommand = new GroupWebServerDispatchCommand(mockGroup, aCommand,
                testUser, groupControlId);
        verify(mockCommandDispatchGateway).asyncDispatchCommand(dispatchCommand);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDispatchCommandComplete() {
        List<WebServerDispatchCommandResult> results = new ArrayList<>();
        Identifier<WebServerControlHistory> webServerControlId = new Identifier<>((long) 3);
        GroupWebServerDispatchCommand groupWebServerDispatchCommand = new GroupWebServerDispatchCommand(mockGroup,
                aCommand, testUser, groupControlId);
        WebServerDispatchCommandResult commandResult = new WebServerDispatchCommandResult(true, webServerControlId,
                groupWebServerDispatchCommand);
        results.add(commandResult);

        cut.dispatchCommandComplete(results);

//        verify(mockPersistenceService).completeControlHistoryEvent(any(Event.class));
    }

}
