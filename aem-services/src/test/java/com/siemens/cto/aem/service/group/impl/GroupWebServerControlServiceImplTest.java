package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.domain.command.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.command.webserver.ControlGroupWebServerCommand;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class GroupWebServerControlServiceImplTest {
    private GroupControlPersistenceService mockPersistenceService;
    private GroupService mockGroupService;
    private CommandDispatchGateway mockCommandDispatchGateway;

    private GroupWebServerControlServiceImpl cut;
    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private Group mockGroup;
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
        aCommand = new ControlGroupWebServerCommand(groupId, WebServerControlOperation.START);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWithInvalidGroup() {
        ControlGroupWebServerCommand aCommand = new ControlGroupWebServerCommand(null, WebServerControlOperation.START);
        cut.controlGroup(aCommand, testUser);
    }

    @Test
    public void testControlGroup() {
        cut.controlGroup(aCommand, testUser);

        GroupWebServerDispatchCommand dispatchCommand = new GroupWebServerDispatchCommand(mockGroup, aCommand, testUser);
        verify(mockCommandDispatchGateway).asyncDispatchCommand(dispatchCommand);
    }

    @Test
    public void testDispatchCommandComplete() {
        List<WebServerDispatchCommandResult> results = new ArrayList<>();
        GroupWebServerDispatchCommand groupWebServerDispatchCommand = new GroupWebServerDispatchCommand(mockGroup,
                aCommand, testUser);
        WebServerDispatchCommandResult commandResult = new WebServerDispatchCommandResult(true, groupWebServerDispatchCommand);
        results.add(commandResult);

        cut.dispatchCommandComplete(results);
    }

}
