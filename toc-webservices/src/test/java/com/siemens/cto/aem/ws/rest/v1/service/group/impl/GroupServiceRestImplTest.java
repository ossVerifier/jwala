package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.group.AddJvmsToGroupRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
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
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import org.apache.cxf.jaxrs.ext.MessageContext;
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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import static com.siemens.cto.aem.common.domain.model.id.Identifier.id;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
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

    private GroupServiceImpl groupService;
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
    private Group mockLiteGroup;

    private Set<Group> liteGroups;

    @Mock
    private Group mockGroup;

    @InjectMocks
    @Spy
    private GroupServiceRestImpl groupServiceRest = new GroupServiceRestImpl(groupService = Mockito.mock(GroupServiceImpl.class), resourceService = Mockito.mock(ResourceServiceImpl.class));

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
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetGroupList() {
        when(groupService.getGroups(eq(false))).thenReturn(groupList);

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
        when(groupService.findGroups(any(String.class))).thenReturn(groupList);

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
        when(groupService.getGroup(any(Identifier.class))).thenReturn(group);

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
        when(groupService.createGroup(any(CreateGroupRequest.class), any(User.class))).thenReturn(group);

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
        when(groupService.updateGroup(any(UpdateGroupRequest.class), any(User.class))).thenReturn(group);
        when(groupService.getGroup(eq("currentName"))).thenReturn(group);

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
        verify(groupService, atLeastOnce()).removeGroup(eq("groupName"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);

        response = groupServiceRest.removeGroup("1", false);
        Identifier<Group> groupIdentifier = new Identifier<Group>("1");
        verify(groupService, atLeastOnce()).removeGroup(eq(groupIdentifier));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddJvmsToGroup() {
        when(groupService.addJvmsToGroup(any(AddJvmsToGroupRequest.class), any(User.class))).thenReturn(group);

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
        when(groupService.removeJvmFromGroup(any(RemoveJvmFromGroupRequest.class), any(User.class))).thenReturn(group);

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
        when(groupService.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);

        when(groupService.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
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
        when(groupService.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);
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
        when(groupService.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
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
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mockJvm);
        Collection<ResourceType> resourceTypes = new ArrayList<>();
        ResourceType mockResource = mock(ResourceType.class);
        resourceTypes.add(mockResource);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockResource.getEntityType()).thenReturn("jvm");
        when(mockResource.getConfigFileName()).thenReturn("server.xml");
        when(mockResource.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        when(groupService.getGroup(group.getId())).thenReturn(mockGroup);
        when(groupService.populateJvmConfig(any(Identifier.class), anyList(), any(User.class), anyBoolean())).thenReturn(group);
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
    public void testPopulateWebServerConfig() {
        Set<WebServer> wsSet = new HashSet<>();
        wsSet.add(mockWebServer);
        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        when(groupService.getGroupWithWebServers(group.getId())).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(wsSet);
        when(groupService.populateWebServerConfig(any(Identifier.class), anyList(), any(User.class), anyBoolean())).thenReturn(group);
        Response response = groupServiceRest.populateWebServerConfig(group.getId(), authenticatedUser, false);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroupWebServers() {
        JsonControlWebServer mockControlWebServer = mock(JsonControlWebServer.class);
        when(mockControlWebServer.toControlOperation()).thenReturn(WebServerControlOperation.START);
        Response response = groupServiceRest.controlGroupWebservers(group.getId(), mockControlWebServer, authenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroup() {
        JsonControlGroup mockControlGroup = mock(JsonControlGroup.class);
        when(mockControlGroup.toControlOperation()).thenReturn(GroupControlOperation.START);
        Response response = groupServiceRest.controlGroup(group.getId(), mockControlGroup, authenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetCurrentJvmStates() {
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

    @Test
    public void testControlGroupJvms() {
        Response response = groupServiceRest.controlGroupJvms(group.getId(), new JsonControlJvm(JvmControlOperation.START.getExternalValue()), authenticatedUser);
        assertNotNull(response);
    }

    @Test(expected = InternalErrorException.class)
    public void testUploadWebServerTemplate() {
        final MessageContext mockContext = mock(MessageContext.class);
        final HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
        when(mockContext.getHttpHeaders()).thenReturn(mockHttpHeaders);
        final ArrayList<MediaType> mediaList = new ArrayList<>();
        mediaList.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHttpHeaders.getAcceptableMediaTypes()).thenReturn(mediaList);
        HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
        when(mockServletRequest.getContentType()).thenReturn("text");
        when(mockContext.getHttpServletRequest()).thenReturn(mockServletRequest);
        groupServiceRest.setMessageContext(mockContext);
        Response response = groupServiceRest.uploadGroupWebServerConfigTemplate(group.getName(), authenticatedUser, "httpd.conf");
        assertNotNull(response);
    }

    @Test
    public void testUpdateWebServerTemplate() {
        Response response = groupServiceRest.updateGroupWebServerResourceTemplate(group.getName(), "httpd.conf", "httpd.conf content for testing");
        assertNotNull(response);

        when(groupService.updateGroupWebServerResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("test webServer", "httpd.conf"));
        response = groupServiceRest.updateGroupWebServerResourceTemplate(group.getName(), "httpd.conf", "httpd.conf content for testing");
        assertNotNull(response);
    }

    @Test
    public void testPreviewWebServerTemplate() {
        Response response = groupServiceRest.previewGroupWebServerResourceTemplate(group.getName(), "httpd.conf");
        assertNotNull(response);
    }

    @Test
    public void testGetWebServerTemplate() {
        Response response = groupServiceRest.getGroupWebServerResourceTemplate(group.getName(), "httpd.conf", false);
        assertNotNull(response);
    }

    @Test
    public void testGenerateAndDeployWebServerFiles() {
        Set<WebServer> emptyWsSet = new HashSet<>();
        when(groupService.getGroup(anyString())).thenReturn(mockGroup);
        when(groupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(emptyWsSet);
        Response response = groupServiceRest.generateAndDeployGroupWebServersFile(group.getName(), authenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testGetWebserverResourceNames() {
        Response response = groupServiceRest.getGroupWebServersResourceNames(group.getName());
        assertNotNull(response);
    }

    @Test
    public void testGetJvmResourceNames() {
        Response response = groupServiceRest.getGroupJvmsResourceNames(group.getName(), false);
        assertNotNull(response);
    }

    @Test
    public void testPreviewJvmTemplate() {
        Response response = groupServiceRest.previewGroupJvmResourceTemplate(group.getName(), "server.xml");
        assertNotNull(response);
    }

    @Test
    public void testUpdateJvmTemplate() {
        Response response = groupServiceRest.updateGroupJvmResourceTemplate(group.getName(), "server.xml", "test server.xml content");
        assertNotNull(response);

        when(groupService.updateGroupJvmResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("test jvm", "server.xml"));
        response = groupServiceRest.updateGroupJvmResourceTemplate(group.getName(), "server.xml", "server.xml content for testing");
        assertNotNull(response);

    }

    @Test(expected = InternalErrorException.class)
    public void testUploadJvmTemplate() {
        final MessageContext mockContext = mock(MessageContext.class);
        final HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
        when(mockContext.getHttpHeaders()).thenReturn(mockHttpHeaders);
        final ArrayList<MediaType> mediaList = new ArrayList<>();
        mediaList.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHttpHeaders.getAcceptableMediaTypes()).thenReturn(mediaList);
        HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
        when(mockServletRequest.getContentType()).thenReturn("text");
        when(mockContext.getHttpServletRequest()).thenReturn(mockServletRequest);
        groupServiceRest.setMessageContext(mockContext);
        Response response = groupServiceRest.uploadGroupJvmConfigTemplate(group.getName(), authenticatedUser, "server.xml");
        assertNotNull(response);
    }

    @Test
    public void testGetJvmTemplate() {
        Response response = groupServiceRest.getGroupJvmResourceTemplate(group.getName(), "server.xml", false);
        assertNotNull(response);
    }

    @Test
    public void testGenerateAndDeployJvmTemplate() {
        Set<WebServer> emptyWsSet = new HashSet<>();
        when(groupService.getGroup(anyString())).thenReturn(mockGroup);
        when(groupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(emptyWsSet);
        Response response = groupServiceRest.generateAndDeployGroupJvmFile(group.getName(), "server.xml", authenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testPopulateGroupJvmTemplate() {
        Collection<ResourceType> resourceTypesList = new ArrayList<>();
        ResourceType mockResourceType = mock(ResourceType.class);
        when(mockResourceType.getEntityType()).thenReturn("jvm");
        when(mockResourceType.getConfigFileName()).thenReturn("server.xml");
        when(mockResourceType.getTemplateName()).thenReturn("ServerXMLTemplate.tpl");
        resourceTypesList.add(mockResourceType);
        when(resourceService.getResourceTypes()).thenReturn(resourceTypesList);
        Response response = groupServiceRest.populateGroupJvmTemplates(group.getName(), authenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testPopulateGroupWebServerTemplate() {
        Collection<ResourceType> resourceTypesList = new ArrayList<>();
        ResourceType mockResourceType = mock(ResourceType.class);
        when(mockResourceType.getEntityType()).thenReturn("webServer");
        when(mockResourceType.getConfigFileName()).thenReturn("httpd.conf");
        when(mockResourceType.getTemplateName()).thenReturn("HttpdSslConfTemplate.tpl");
        resourceTypesList.add(mockResourceType);
        when(resourceService.getResourceTypes()).thenReturn(resourceTypesList);
        Response response = groupServiceRest.populateGroupWebServerTemplates(group.getName(), authenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testUploadGroupWebServerConfigTemplate() throws Exception {
        final MessageContext msgContextMock = mock(MessageContext.class);
        final HttpHeaders httpHeadersMock = mock(HttpHeaders.class);
        final List<MediaType> mediaTypeList = new ArrayList<>();
        final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
        when(httpHeadersMock.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(msgContextMock.getHttpHeaders()).thenReturn(httpHeadersMock);
        when(msgContextMock.getHttpServletRequest()).thenReturn(httpServletRequestMock);
        when(msgContextMock.getHttpServletResponse()).thenReturn(httpServletResponseMock);
        when(httpServletRequestMock.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundaryXRxegBGqTe4gApI2");
        when(httpServletRequestMock.getInputStream()).thenReturn(new DelegatingServletInputStream());
        groupServiceRest.setMessageContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        groupServiceRest.uploadGroupWebServerConfigTemplate("any", authenticatedUser, "any");
        verify(groupService).populateGroupWebServerTemplates(anyString(), anyList(), any(User.class));
    }

    @Test
    public void testUploadGroupAppConfigTemplate() throws Exception {
        final MessageContext msgContextMock = mock(MessageContext.class);
        final HttpHeaders httpHeadersMock = mock(HttpHeaders.class);
        final List<MediaType> mediaTypeList = new ArrayList<>();
        final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
        when(httpHeadersMock.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(msgContextMock.getHttpHeaders()).thenReturn(httpHeadersMock);
        when(msgContextMock.getHttpServletRequest()).thenReturn(httpServletRequestMock);
        when(msgContextMock.getHttpServletResponse()).thenReturn(httpServletResponseMock);
        when(httpServletRequestMock.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundaryXRxegBGqTe4gApI2");
        when(httpServletRequestMock.getInputStream()).thenReturn(new DelegatingServletInputStream());
        groupServiceRest.setMessageContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        groupServiceRest.uploadGroupAppConfigTemplate("any", authenticatedUser, "any");
        verify(groupService).populateGroupAppTemplate(anyString(), anyString(), anyString());
    }

    @Test
    public void testUpdateGroupAppTemplate() {
        when(groupService.updateGroupAppResourceTemplate(anyString(), anyString(), anyString())).thenReturn("new hct.xml content");
        groupServiceRest.updateGroupAppResourceTemplate("testGroup", "hct.xml", "new hct.xml context");
        verify(groupService).updateGroupAppResourceTemplate(anyString(), anyString(), anyString());

        when(groupService.updateGroupAppResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("testApp", "hct.xml"));
        Response response = groupServiceRest.updateGroupAppResourceTemplate("testGroup", "hct.xml", "newer hct.xml content");
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testUploadGroupJvmConfigTemplate() throws Exception {
        final MessageContext msgContextMock = mock(MessageContext.class);
        final HttpHeaders httpHeadersMock = mock(HttpHeaders.class);
        final List<MediaType> mediaTypeList = new ArrayList<>();
        final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
        when(httpHeadersMock.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(msgContextMock.getHttpHeaders()).thenReturn(httpHeadersMock);
        when(msgContextMock.getHttpServletRequest()).thenReturn(httpServletRequestMock);
        when(msgContextMock.getHttpServletResponse()).thenReturn(httpServletResponseMock);
        when(httpServletRequestMock.getContentType()).thenReturn("multipart/form-data; boundary=----WebKitFormBoundaryXRxegBGqTe4gApI2");
        when(httpServletRequestMock.getInputStream()).thenReturn(new DelegatingServletInputStream());
        groupServiceRest.setMessageContext(msgContextMock);

        final SecurityContext securityContextMock = mock(SecurityContext.class);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(securityContextMock);

        groupServiceRest.uploadGroupJvmConfigTemplate("any", authenticatedUser, "any");
        verify(groupService).populateGroupJvmTemplates(anyString(), anyList(), any(User.class));
    }

    @Test
    public void testPreviewGroupAppResourceTemplate() {
        groupServiceRest.previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!");
        verify(groupService).previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!");
    }

    @Test
    public void testGetGroupAppResourceTemplate() {
        groupServiceRest.getGroupAppResourceTemplate("testGroup", "hct.xml", false);
        verify(groupService).getGroupAppResourceTemplate("testGroup", "hct.xml", false);
    }

    @Test
    public void testGetGroupAppResourceNames() {
        groupServiceRest.getGroupAppResourceNames("testGroup");
        verify(groupService).getGroupAppsResourceTemplateNames("testGroup");
    }



    /**
     * Instead of mocking the ServletInputStream, let's extend it instead.
     *
     * @see "http://stackoverflow.com/questions/20995874/how-to-mock-a-javax-servlet-servletinputstream"
     */
    static class DelegatingServletInputStream extends ServletInputStream {

        private InputStream inputStream;

        public DelegatingServletInputStream() {
            inputStream = new ByteArrayInputStream("------WebKitFormBoundaryXRxegBGqTe4gApI2\r\nContent-Disposition: form-data; name=\"hct.properties\"; filename=\"hotel-booking.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\n------WebKitFormBoundaryXRxegBGqTe4gApI2--".getBytes(Charset.defaultCharset()));
        }

        /**
         * Return the underlying source stream (never <code>null</code>).
         */
        public final InputStream getSourceStream() {
            return inputStream;
        }


        public int read() throws IOException {
            return inputStream.read();
        }

        public void close() throws IOException {
            super.close();
            inputStream.close();
        }

    }

}
