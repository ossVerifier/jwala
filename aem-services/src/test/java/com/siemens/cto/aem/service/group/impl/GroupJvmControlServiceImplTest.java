package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
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

    private GroupControlPersistenceService mockPersistenceService;
    private GroupService mockGroupService;
    private CommandDispatchGateway mockCommandDispatchGateway;

    private GroupJvmControlServiceImpl cut;
    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private Group mockGroup;
    private User testUser = new User("testUser");
    private ControlGroupJvmCommand aCommand;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockPersistenceService = mock(GroupControlPersistenceService.class);
        mockGroupService = mock(GroupService.class);
        mockCommandDispatchGateway = mock(CommandDispatchGateway.class);

        cut = new GroupJvmControlServiceImpl(mockPersistenceService, mockGroupService, mockCommandDispatchGateway);

        mockGroup = mock(Group.class);
        aCommand = new ControlGroupJvmCommand(groupId, JvmControlOperation.START);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWithInvalidGroup() {
        ControlGroupJvmCommand aCommand = new ControlGroupJvmCommand(null, JvmControlOperation.START);
        cut.controlGroup(aCommand, testUser);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDispatchCommandComplete() {
        List<JvmDispatchCommandResult> results = new ArrayList<>();
        GroupJvmDispatchCommand groupJvmDispatchCommand = new GroupJvmDispatchCommand(mockGroup, aCommand, testUser);
        JvmDispatchCommandResult commandResult = new JvmDispatchCommandResult(true, groupJvmDispatchCommand);
        results.add(commandResult);
        
        cut.dispatchCommandComplete(results);
        
    }
}
