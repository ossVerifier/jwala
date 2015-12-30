package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.request.group.ControlGroupJvmRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.common.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class GroupJvmControlServiceImplTest {

    private GroupService mockGroupService;
    private CommandDispatchGateway mockCommandDispatchGateway;

    private GroupJvmControlServiceImpl cut;
    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private Group mockGroup;
    private User testUser = new User("testUser");
    private ControlGroupJvmRequest controlGroupJvmRequest;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockGroupService = mock(GroupService.class);
        mockCommandDispatchGateway = mock(CommandDispatchGateway.class);

        cut = new GroupJvmControlServiceImpl(mockGroupService, mockCommandDispatchGateway);

        mockGroup = mock(Group.class);
        controlGroupJvmRequest = new ControlGroupJvmRequest(groupId, JvmControlOperation.START);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWithInvalidGroup() {
        ControlGroupJvmRequest controlGroupJvmRequest = new ControlGroupJvmRequest(null, JvmControlOperation.START);
        cut.controlGroup(controlGroupJvmRequest, testUser);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDispatchCommandComplete() {
        List<JvmDispatchCommandResult> results = new ArrayList<>();
        GroupJvmDispatchCommand groupJvmDispatchCommand = new GroupJvmDispatchCommand(mockGroup, controlGroupJvmRequest, testUser);
        JvmDispatchCommandResult commandResult = new JvmDispatchCommandResult(true, groupJvmDispatchCommand);
        results.add(commandResult);
        
        cut.dispatchCommandComplete(results);
        
    }
}
