package com.siemens.cto.aem.service.group.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.dispatch.GroupJvmDispatchCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupService;

public class GroupJvmControlServiceImplTest {

    private GroupControlPersistenceService mockPersistenceService;
    private GroupService mockGroupService;
    private CommandDispatchGateway mockCommandDispatchGateway;

    private GroupJvmControlServiceImpl cut;
    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private Group mockGroup;
    private GroupControlHistory mockGroupControlHistory;
    private Identifier<GroupControlHistory> groupControlHistoryId;
    private User testUser = new User("testUser");

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockPersistenceService = mock(GroupControlPersistenceService.class);
        mockGroupService = mock(GroupService.class);
        mockCommandDispatchGateway = mock(CommandDispatchGateway.class);

        cut = new GroupJvmControlServiceImpl(mockPersistenceService, mockGroupService, mockCommandDispatchGateway);

        mockGroup = mock(Group.class);
        mockGroupControlHistory = mock(GroupControlHistory.class);
        groupControlHistoryId = new Identifier<>((long) 10);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
        when(mockPersistenceService.addIncompleteControlHistoryEvent(any(Event.class))).thenReturn(
                mockGroupControlHistory);
        when(mockGroupControlHistory.getId()).thenReturn(groupControlHistoryId);
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWithInvalidGroup() {
        ControlGroupJvmCommand aCommand = new ControlGroupJvmCommand(null, JvmControlOperation.START);
        cut.controlGroup(aCommand, testUser);
    }

    @Test
    public void testControlGroup() {
        ControlGroupJvmCommand aCommand = new ControlGroupJvmCommand(groupId, JvmControlOperation.START);
        GroupControlHistory groupControlHistory = cut.controlGroup(aCommand, testUser);
        assertNotNull(groupControlHistory);
        
        GroupJvmDispatchCommand dispatchCommand = new GroupJvmDispatchCommand(mockGroup, aCommand, testUser,
                groupControlHistoryId);
        verify(mockCommandDispatchGateway).asyncDispatchCommand(dispatchCommand);
    }
}
