package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupControlOperation;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.group.ControlGroupJvmRequest;
import com.cerner.jwala.common.request.group.ControlGroupRequest;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import com.cerner.jwala.service.group.impl.GroupControlServiceImpl;
import com.cerner.jwala.service.group.impl.GroupJvmControlServiceImpl;
import com.cerner.jwala.service.state.GroupStateService.API;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;

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
        
        cut = new GroupControlServiceImpl(mockGroupWebServerControlService, mockGroupJvmControlService);
        
    }

    @Test
    public void testControlGroup() {
        ControlGroupRequest controlGroupRequest= new ControlGroupRequest(testGroupId, GroupControlOperation.START);
        when(mockGroupStateService.canStart(testGroupId, systemUser)).thenReturn(true);
        
        cut.controlGroup(controlGroupRequest, systemUser);
        
        ControlGroupWebServerRequest wsCommand = new ControlGroupWebServerRequest(testGroupId, WebServerControlOperation.START);
        verify(mockGroupWebServerControlService).controlGroup(wsCommand, systemUser);
        
        ControlGroupJvmRequest jvmCommand = new ControlGroupJvmRequest(testGroupId, JvmControlOperation.START);
        verify(mockGroupJvmControlService).controlGroup(jvmCommand, systemUser);
        
    }

    @Test(expected = BadRequestException.class)
    @Ignore
    // TODO: Fix this!
    public void testControlGroupWhenBadState() {
        ControlGroupRequest controlGroupRequest= new ControlGroupRequest(testGroupId, GroupControlOperation.START);
        when(mockGroupStateService.canStart(testGroupId, systemUser)).thenReturn(false);
        
        cut.controlGroup(controlGroupRequest, systemUser);
    }

}
