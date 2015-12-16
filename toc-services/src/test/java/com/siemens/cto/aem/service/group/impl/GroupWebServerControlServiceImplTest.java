package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.common.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
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
    private ControlGroupWebServerRequest controlGroupWebServerRequest;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockPersistenceService = mock(GroupControlPersistenceService.class);
        mockGroupService = mock(GroupService.class);
        mockCommandDispatchGateway = mock(CommandDispatchGateway.class);

        cut = new GroupWebServerControlServiceImpl(mockPersistenceService, mockGroupService, mockCommandDispatchGateway);

        mockGroup = mock(Group.class);
        controlGroupWebServerRequest = new ControlGroupWebServerRequest(groupId, WebServerControlOperation.START);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWithInvalidGroup() {
        ControlGroupWebServerRequest controlGroupWebServerRequest = new ControlGroupWebServerRequest(null, WebServerControlOperation.START);
        cut.controlGroup(controlGroupWebServerRequest, testUser);
    }

    @Test
    public void testControlGroup() {
        cut.controlGroup(controlGroupWebServerRequest, testUser);

        GroupWebServerDispatchCommand dispatchCommand = new GroupWebServerDispatchCommand(mockGroup, controlGroupWebServerRequest, testUser);
        verify(mockCommandDispatchGateway).asyncDispatchCommand(dispatchCommand);
    }

    @Test
    public void testDispatchCommandComplete() {
        List<WebServerDispatchCommandResult> results = new ArrayList<>();
        GroupWebServerDispatchCommand groupWebServerDispatchCommand = new GroupWebServerDispatchCommand(mockGroup,
                controlGroupWebServerRequest, testUser);
        WebServerDispatchCommandResult commandResult = new WebServerDispatchCommandResult(true, groupWebServerDispatchCommand);
        results.add(commandResult);

        cut.dispatchCommandComplete(results);
    }

}
