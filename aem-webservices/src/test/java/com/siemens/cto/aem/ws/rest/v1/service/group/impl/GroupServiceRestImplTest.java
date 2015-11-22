package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.common.AemConstants;
import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.group.impl.GroupControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupJvmControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupServiceImpl;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.GroupIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupChildType;
import com.siemens.cto.aem.ws.rest.v1.service.group.MembershipDetails;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.*;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.*;

/**
 * @author meleje00
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupServiceRestImplTest {

    private static final String GROUP_CONTROL_TEST_USERNAME = "groupControlTest";
    private static final String name = "groupName";
    private static final List<Group> groupList = createGroupList();
    private static final Group group = groupList.get(0);

    private GroupServiceImpl impl;
    private ResourceService resourceService;

    @Mock
    private GroupControlServiceImpl controlImpl;

    @Mock
    private GroupJvmControlServiceImpl controlJvmImpl;

    @Mock
    private StateService<Group, GroupState> groupStateService;

    @Mock
    private GroupWebServerControlService groupWSControlService;

    @Mock
    private AuthenticatedUser authenticatedUser;

    @Mock
    private GroupPersistenceService groupPersistenceService;

    @Mock
    private Jvm mockJvm;

    private List<Jvm> jvms;

    @Mock
    private WebServer mockWebServer;

    private List<WebServer> webServers;

    private Set<Group> groups;

    @Mock
    private LiteGroup mockLiteGroup;

    private Set<LiteGroup> liteGroups;

    @Mock
    private Group mockGroup;

    @InjectMocks
    @Spy
    private GroupServiceRestImpl groupServiceRest = new GroupServiceRestImpl(impl = Mockito.mock(GroupServiceImpl.class), resourceService = Mockito.mock(ResourceServiceImpl.class));

    private static List<Group> createGroupList() {
        final Group ws = new Group(Identifier.id(1L, Group.class), name);
        final List<Group> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setup() {
        when(authenticatedUser.getUser()).thenReturn(new User(GROUP_CONTROL_TEST_USERNAME));

        when(mockJvm.getJvmName()).thenReturn("jvm1");
        when(mockLiteGroup.getName()).thenReturn("group1");
        liteGroups = new HashSet<>();
        liteGroups.add(mockLiteGroup);
        when(mockJvm.getGroups()).thenReturn(liteGroups);
        jvms = new ArrayList<>();
        jvms.add(mockJvm);

        when(mockWebServer.getName()).thenReturn("webServer1");
        when(mockGroup.getName()).thenReturn("group2");
        groups = new HashSet<>();
        groups.add(mockGroup);
        when(mockWebServer.getGroups()).thenReturn(groups);
        webServers = new ArrayList<>();
        webServers.add(mockWebServer);
    }

    @After
    public void cleanUp() {
        System.clearProperty(AemConstants.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetGroupList() {
        when(impl.getGroups(eq(false))).thenReturn(groupList);

        final NameSearchParameterProvider nameSearchParameterProvider = new NameSearchParameterProvider();
        final Response response = groupServiceRest.getGroups(nameSearchParameterProvider, false);
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
        when(impl.findGroups(any(String.class))).thenReturn(groupList);

        final NameSearchParameterProvider nameSearchParameterProvider = new NameSearchParameterProvider(name);

        final Response response = groupServiceRest.getGroups(nameSearchParameterProvider, false);
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

        final Response response = groupServiceRest.getGroup("1", false);
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

        final Response response = groupServiceRest.createGroup(name, authenticatedUser);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testUpdateGroup() {
        final JsonUpdateGroup jsonUpdateGroup = new JsonUpdateGroup("currentName", name);
        when(impl.updateGroup(any(UpdateGroupCommand.class), any(User.class))).thenReturn(group);
        when(impl.getGroup(eq("currentName"))).thenReturn(group);

        final Response response = groupServiceRest.updateGroup(jsonUpdateGroup, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testRemoveGroup() {
        Response response = groupServiceRest.removeGroup("groupName", true);
        verify(impl, atLeastOnce()).removeGroup(eq("groupName"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);

        response = groupServiceRest.removeGroup("1", false);
        Identifier<Group> groupIdentifier = new Identifier<Group>("1");
        verify(impl, atLeastOnce()).removeGroup(eq(groupIdentifier));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddJvmsToGroup() {
        when(impl.addJvmsToGroup(any(AddJvmsToGroupCommand.class), any(User.class))).thenReturn(group);

        final Set<String> jvmIds = new HashSet<String>();
        jvmIds.add("1");
        final JsonJvms jsonJvms = new JsonJvms(jvmIds);
        final Response response = groupServiceRest.addJvmsToGroup(Identifier.id(1l, Group.class), jsonJvms, authenticatedUser);
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
                groupServiceRest.removeJvmFromGroup(Identifier.id(1l, Group.class),
                        Identifier.id(1l, Jvm.class), authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testSignalReset() {
        when(this.controlImpl.resetState(eq(Identifier.id(1L, Group.class)), isA(User.class))).thenReturn(new CurrentGroupState(id(1L, Group.class), GroupState.GRP_PARTIAL, DateTime.now()));
        final Response response =
                groupServiceRest.resetState(Identifier.id(1L, Group.class),
                        authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof CurrentGroupState);

        // act of calling the service has been verified because it was stubbed with when.
    }

    @Test
    public void getOtherGroupMembershipDetailsOfTheChildrenChildTypeNull() {
        when(impl.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);

        when(impl.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
        final Response response = groupServiceRest.getOtherGroupMembershipDetailsOfTheChildren(new Identifier<Group>("1"), null);
        assertEquals(response.getStatus(), 200);
        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final List<MembershipDetails> membershipDetailsList = (List) applicationResponse.getApplicationResponseContent();
        assertEquals("jvm1", membershipDetailsList.get(0).getName());
        assertEquals("group1", membershipDetailsList.get(0).getGroupNames().get(0));
        assertEquals("webServer1", membershipDetailsList.get(1).getName());
        assertEquals("group2", membershipDetailsList.get(1).getGroupNames().get(0));
    }

    @Test
    public void getOtherGroupMembershipDetailsOfTheChildrenChildTypeJvm() {
        when(impl.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);
        final Response response =
                groupServiceRest.getOtherGroupMembershipDetailsOfTheChildren(new Identifier<Group>("1"), GroupChildType.JVM);
        assertEquals(response.getStatus(), 200);
        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final List<MembershipDetails> membershipDetailsList = (List) applicationResponse.getApplicationResponseContent();
        assertEquals("jvm1", membershipDetailsList.get(0).getName());
        assertEquals("group1", membershipDetailsList.get(0).getGroupNames().get(0));
    }

    @Test
    public void getOtherGroupMembershipDetailsOfTheChildrenChildTypeWebServer() {
        when(impl.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
        final Response response
                = groupServiceRest.getOtherGroupMembershipDetailsOfTheChildren(new Identifier<Group>("1"), GroupChildType.WEB_SERVER);
        assertEquals(response.getStatus(), 200);
        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final List<MembershipDetails> membershipDetailsList = (List) applicationResponse.getApplicationResponseContent();
        assertEquals("webServer1", membershipDetailsList.get(0).getName());
        assertEquals("group2", membershipDetailsList.get(0).getGroupNames().get(0));
    }

    @Test
    public void testPopulateJvmConfig() {
        System.setProperty(AemConstants.PROPERTIES_ROOT_PATH, "./src/test/resources");
        Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mockJvm);
        Collection<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType mockResource = mock(ResourceType.class);
        resourceTypes.add(mockResource);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockResource.getEntityType()).thenReturn("jvm");
        when(mockResource.getConfigFileName()).thenReturn("server.xml");
        when(mockResource.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        when(impl.getGroup(group.getId())).thenReturn(mockGroup);
        when(impl.populateJvmConfig(any(Identifier.class), anyList(), any(User.class), anyBoolean())).thenReturn(group);
        when(resourceService.getResourceTypes()).thenReturn(resourceTypes);
        Response response = groupServiceRest.populateJvmConfig(group.getId(), authenticatedUser, false);
        assertNotNull(response.getEntity());

        when(mockResource.getTemplateName()).thenReturn("NoTemplate.tpl");
        boolean exceptionThrown = false;
        try {
            groupServiceRest.populateJvmConfig(group.getId(), authenticatedUser, false);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testPopulateWebServerConfig(){
        Set<WebServer> wsSet = new HashSet<>();
        wsSet.add(mockWebServer);
        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(impl.getGroupWithWebServers(group.getId())).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(wsSet);
        when(impl.populateWebServerConfig(any(Identifier.class), anyList(), any(User.class), anyBoolean())).thenReturn(group);
        Response response = groupServiceRest.populateWebServerConfig(group.getId(), authenticatedUser, false);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroupWebServers(){
        JsonControlWebServer mockControlWebServer = mock(JsonControlWebServer.class);
        when(mockControlWebServer.toControlOperation()).thenReturn(WebServerControlOperation.START);
        Response response = groupServiceRest.controlGroupWebservers(group.getId(), mockControlWebServer, authenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroup(){
        JsonControlGroup mockControlGroup = mock(JsonControlGroup.class);
        when(mockControlGroup.toControlOperation()).thenReturn(GroupControlOperation.START);
        Response response = groupServiceRest.controlGroup(group.getId(), mockControlGroup, authenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetCurrentJvmStates(){
        GroupIdsParameterProvider mockGroupIdsParamProvider = mock(GroupIdsParameterProvider.class);
        Set<Identifier<Group>> setGroupIds = new HashSet<>();
        setGroupIds.add(group.getId());
        when(mockGroupIdsParamProvider.valueOf()).thenReturn(setGroupIds);
        when(groupStateService.getCurrentStates(anySet())).thenReturn(new HashSet<CurrentState<Group, GroupState>>());
        Response response = groupServiceRest.getCurrentJvmStates(mockGroupIdsParamProvider);
        assertNotNull(response.getEntity());

        when(mockGroupIdsParamProvider.valueOf()).thenReturn(new HashSet<Identifier<Group>>());
        response = groupServiceRest.getCurrentJvmStates(mockGroupIdsParamProvider);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }
}
