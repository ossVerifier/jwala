package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.group.AddJvmsToGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.group.impl.GroupControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupJvmControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupServiceImpl;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;

/**
 * 
 * @author meleje00
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupServiceRestImplTest {

    private static final String GROUP_CONTROL_TEST_USERNAME = "groupControlTest";
    private static final String name = "groupName";
    private static final List<Group> groupList = createGroupList();
    private static final Group group = groupList.get(0);
    private GroupControlHistory groupControlHistory = createGroupControlHistory(group.getId());
           
    private GroupServiceImpl impl;

    @Mock
    private GroupControlServiceImpl controlImpl;
    
    @Mock
    private GroupJvmControlServiceImpl controlJvmImpl;
    
    @Mock
    private SecurityContext jaxrsSecurityContext;

    @InjectMocks @Spy
    private GroupServiceRestImpl cut = new GroupServiceRestImpl(impl = Mockito.mock(GroupServiceImpl.class));

    private static List<Group> createGroupList() {
        final Group ws = new Group(Identifier.id(1L, Group.class), name);
        final List<Group> result = new ArrayList<Group>();
        result.add(ws);
        return result;
    }
    
    private GroupControlHistory createGroupControlHistory(Identifier<Group> id) {
        GroupControlHistory gch = new GroupControlHistory(
                Identifier.id(1L, GroupControlHistory.class), 
                id, 
                GroupControlOperation.START, 
                AuditEvent.now(new User(GROUP_CONTROL_TEST_USERNAME))
                );
        return gch;
    }

    @Test
    public void testGetGroupList() {
        when(impl.getGroups(any(PaginationParameter.class))).thenReturn(groupList);

        final PaginationParamProvider paginationProvider = new PaginationParamProvider();
        final NameSearchParameterProvider nameSearchParameterProvider = new NameSearchParameterProvider();
        final Response response = cut.getGroups(paginationProvider, nameSearchParameterProvider);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<Group> receivedList = (List<Group>) content;
        final Group received = receivedList.get(0);
        assertEquals(group, received);
    }

    @Test
    public void testGetGroupListName() {
        when(impl.findGroups(any(String.class), any(PaginationParameter.class))).thenReturn(groupList);

        final PaginationParamProvider paginationProvider = new PaginationParamProvider();
        final NameSearchParameterProvider nameSearchParameterProvider = new NameSearchParameterProvider(name);

        final Response response = cut.getGroups(paginationProvider, nameSearchParameterProvider);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<Group> receivedList = (List<Group>) content;
        final Group received = receivedList.get(0);
        assertEquals(group, received);
    }

    @Test
    public void testGetGroup() {
        when(impl.getGroup(any(Identifier.class))).thenReturn(group);

        final Response response = cut.getGroup(Identifier.id(Long.valueOf(1l), Group.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testCreateGroup() {
        when(impl.createGroup(any(CreateGroupCommand.class), any(User.class))).thenReturn(group);

        final Response response = cut.createGroup(name);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testUpdateGroup() {
        final JsonUpdateGroup jsonUpdateGroup = new JsonUpdateGroup("1", name);
        when(impl.updateGroup(any(UpdateGroupCommand.class), any(User.class))).thenReturn(group);

        final Response response = cut.updateGroup(jsonUpdateGroup);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testRemoveGroup() {
        final Response response = cut.removeGroup(Identifier.id(Long.valueOf(1l), Group.class));
        verify(impl, atLeastOnce()).removeGroup(any(Identifier.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);
    }

    @Test
    public void testAddJvmsToGroup() {
        when(impl.addJvmsToGroup(any(AddJvmsToGroupCommand.class), any(User.class))).thenReturn(group);

        final Set<String> jvmIds = new HashSet<String>();
        jvmIds.add("1");
        final JsonJvms jsonJvms = new JsonJvms(jvmIds);
        final Response response = cut.addJvmsToGroup(Identifier.id(Long.valueOf(1l), Group.class), jsonJvms);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testRemoveJvmsFromGroup() {
        when(impl.removeJvmFromGroup(any(RemoveJvmFromGroupCommand.class), any(User.class))).thenReturn(group);

        final Response response =
                cut.removeJvmFromGroup(Identifier.id(Long.valueOf(1l), Group.class),
                        Identifier.id(Long.valueOf(1l), Jvm.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }
    
    @Test
    public void testControlJvmsInGroup() { 
        when(controlJvmImpl.controlGroup(isA(ControlGroupJvmCommand.class), isA(User.class))).thenReturn(groupControlHistory);
        when(jaxrsSecurityContext.getUserPrincipal()).thenReturn(new Principal() {

            @Override
            public String getName() {
                return GROUP_CONTROL_TEST_USERNAME;
            }
        
        });
        final Response response =
                cut.controlGroupJvms(Identifier.id(1L, Group.class),
                        new JsonControlJvm("start"), 
                        jaxrsSecurityContext);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof GroupControlHistory);

        // act of calling the service has been verified because it was stubbed with when. 
    }
}
