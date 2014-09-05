package com.siemens.cto.aem.service.group.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.state.GroupStateService.API;

public class GroupControlServiceImplTest {

    private GroupWebServerControlService mockGroupWebServerControlService;
    private GroupJvmControlService mockGroupJvmControlService;
    private API mockGroupStateService;
    private GroupControlServiceImpl cut;
    private Identifier<Group> testGroupId;
    private User systemUser;

    @Before
    public void setUp() throws Exception {
        mockGroupWebServerControlService = mock(GroupWebServerControlService.class);
        mockGroupJvmControlService = mock(GroupJvmControlServiceImpl.class);
        mockGroupStateService = mock(API.class);
        
        testGroupId = new Identifier<>((long) 3);
        systemUser = User.getSystemUser();
        
        cut = new GroupControlServiceImpl(mockGroupWebServerControlService, mockGroupJvmControlService, mockGroupStateService);
        
    }

    @Test
    public void testControlGroup() {
        ControlGroupCommand aCommand = new ControlGroupCommand(testGroupId, GroupControlOperation.START);
        when(mockGroupStateService.canStart(testGroupId, systemUser)).thenReturn(true);
        
        cut.controlGroup(aCommand, systemUser);
        
        ControlGroupWebServerCommand wsCommand = new ControlGroupWebServerCommand(testGroupId, WebServerControlOperation.START);
        verify(mockGroupWebServerControlService).controlGroup(wsCommand, systemUser);
        
        ControlGroupJvmCommand jvmCommand = new ControlGroupJvmCommand(testGroupId, JvmControlOperation.START);
        verify(mockGroupJvmControlService).controlGroup(jvmCommand, systemUser);
        
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWhenBadState() {
        ControlGroupCommand aCommand = new ControlGroupCommand(testGroupId, GroupControlOperation.START);
        when(mockGroupStateService.canStart(testGroupId, systemUser)).thenReturn(false);
        
        cut.controlGroup(aCommand, systemUser);        
    }

}
