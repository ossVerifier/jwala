package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.group.AddJvmsToGroupRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.group.impl.GroupControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupJvmControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupServiceImpl;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupChildType;
import com.siemens.cto.aem.ws.rest.v1.service.group.MembershipDetails;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link GroupServiceRestImpl}.
 *
 * @author meleje00
 */
public class GroupServiceRestImplTest {

    private static final String GROUP_CONTROL_TEST_USERNAME = "groupControlTest";
    private static final String name = "groupName";
    private static final List<Group> groupList = createGroupList();
    private static final Group group = groupList.get(0);

    @Mock
    private GroupServiceImpl mockGroupService;

    @Mock
    private ResourceService mockResourceService;

    @Mock
    private JvmService mockJvmService;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private GroupControlServiceImpl mockControlImpl;

    @Mock
    private GroupJvmControlServiceImpl mockControlJvmImpl;

    @Mock
    private GroupWebServerControlService mockGroupWSControlService;

    @Mock
    private AuthenticatedUser mockAuthenticatedUser;

    @Mock
    private GroupPersistenceService mockGroupPersistenceService;

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

    private GroupServiceRestImpl groupServiceRest;

    @Mock
    private GroupControlService mockGroupControlService;

    @Mock
    private GroupJvmControlService mockGroupJvmControlService;

    @Mock
    private ApplicationService mockApplicationService;

    private static List<Group> createGroupList() {
        final Group ws = new Group(Identifier.id(1L, Group.class), name);
        final List<Group> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockAuthenticatedUser.getUser()).thenReturn(new User(GROUP_CONTROL_TEST_USERNAME));

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

        groupServiceRest = new GroupServiceRestImpl(mockGroupService, mockResourceService, mockGroupControlService,
                mockGroupJvmControlService, mockGroupWSControlService, mockJvmService, mockWebServerService, mockApplicationService);
    }

    @After
    public void cleanUp() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetGroupList() {
        when(mockGroupService.getGroups(eq(false))).thenReturn(groupList);

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
        when(mockGroupService.findGroups(any(String.class))).thenReturn(groupList);

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
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(group);

        Response response = groupServiceRest.getGroup("1", false);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);

        when(mockGroupService.getGroup(anyString())).thenReturn(group);
        response = groupServiceRest.getGroup("1", true);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateGroup() {
        when(mockGroupService.createGroup(any(CreateGroupRequest.class), any(User.class))).thenReturn(group);

        final Response response = groupServiceRest.createGroup(name, mockAuthenticatedUser);
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
        when(mockGroupService.updateGroup(any(UpdateGroupRequest.class), any(User.class))).thenReturn(group);
        when(mockGroupService.getGroup(eq("currentName"))).thenReturn(group);

        final Response response = groupServiceRest.updateGroup(jsonUpdateGroup, mockAuthenticatedUser);
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
        verify(mockGroupService, atLeastOnce()).removeGroup(eq("groupName"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);

        response = groupServiceRest.removeGroup("1", false);
        Identifier<Group> groupIdentifier = new Identifier<Group>("1");
        verify(mockGroupService, atLeastOnce()).removeGroup(eq(groupIdentifier));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddJvmsToGroup() {
        when(mockGroupService.addJvmsToGroup(any(AddJvmsToGroupRequest.class), any(User.class))).thenReturn(group);

        final Set<String> jvmIds = new HashSet<String>();
        jvmIds.add("1");
        final JsonJvms jsonJvms = new JsonJvms(jvmIds);
        final Response response = groupServiceRest.addJvmsToGroup(Identifier.id(1l, Group.class), jsonJvms, mockAuthenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void testRemoveJvmsFromGroup() {
        when(mockGroupService.removeJvmFromGroup(any(RemoveJvmFromGroupRequest.class), any(User.class))).thenReturn(group);

        final Response response =
                groupServiceRest.removeJvmFromGroup(Identifier.id(1l, Group.class),
                        Identifier.id(1l, Jvm.class), mockAuthenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Group);

        final Group received = (Group) content;
        assertEquals(group, received);
    }

    @Test
    public void getOtherGroupMembershipDetailsOfTheChildrenChildTypeNull() {
        when(mockGroupService.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);

        when(mockGroupService.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
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
        when(mockGroupService.getOtherGroupingDetailsOfJvms(any(Identifier.class))).thenReturn(jvms);
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
        when(mockGroupService.getOtherGroupingDetailsOfWebServers(any(Identifier.class))).thenReturn(webServers);
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
        when(mockGroupService.getGroup(group.getId())).thenReturn(mockGroup);
        when(mockGroupService.populateJvmConfig(any(Identifier.class), anyList(), any(User.class), anyBoolean())).thenReturn(group);
        when(mockResourceService.getResourceTypes()).thenReturn(resourceTypes);
        Response response = groupServiceRest.populateJvmConfig(group.getId(), mockAuthenticatedUser, false);
        assertNotNull(response.getEntity());

        when(mockResource.getTemplateName()).thenReturn("NoTemplate.tpl");
        boolean exceptionThrown = false;
        try {
            groupServiceRest.populateJvmConfig(group.getId(), mockAuthenticatedUser, false);
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
        when(mockGroupService.getGroupWithWebServers(group.getId())).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(wsSet);
        when(mockGroupService.populateWebServerConfig(any(Identifier.class), anyList(), any(User.class), anyBoolean())).thenReturn(group);
        Response response = groupServiceRest.populateWebServerConfig(group.getId(), mockAuthenticatedUser, false);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroupWebServers() {
        JsonControlWebServer mockControlWebServer = mock(JsonControlWebServer.class);
        when(mockControlWebServer.toControlOperation()).thenReturn(WebServerControlOperation.START);
        Response response = groupServiceRest.controlGroupWebservers(group.getId(), mockControlWebServer, mockAuthenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroup() {
        JsonControlGroup mockControlGroup = mock(JsonControlGroup.class);
        when(mockControlGroup.toControlOperation()).thenReturn(GroupControlOperation.START);
        Response response = groupServiceRest.controlGroup(group.getId(), mockControlGroup, mockAuthenticatedUser);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testControlGroupJvms() {
        Response response = groupServiceRest.controlGroupJvms(group.getId(), new JsonControlJvm(JvmControlOperation.START.getExternalValue()), mockAuthenticatedUser);
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
        Response response = groupServiceRest.uploadGroupWebServerConfigTemplate(group.getName(), mockAuthenticatedUser, "httpd.conf");
        assertNotNull(response);
    }

    @Test
    public void testUpdateWebServerTemplate() {

        Group mockGroupWithWebServers = mock(Group.class);
        when(mockGroupService.updateGroupWebServerResourceTemplate(anyString(), anyString(), anyString())).thenReturn("httpd.conf content for testing");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroupWithWebServers);
        when(mockGroupWithWebServers.getWebServers()).thenReturn(new HashSet<>(webServers));
        when(mockWebServerService.updateResourceTemplate(anyString(), anyString(), anyString())).thenReturn("httpd.conf content for testing");

        Response response = groupServiceRest.updateGroupWebServerResourceTemplate(group.getName(), "httpd.conf", "httpd.conf content for testing");
        assertNotNull(response);

        when(mockGroupService.updateGroupWebServerResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("test webServer", "httpd.conf"));
        response = groupServiceRest.updateGroupWebServerResourceTemplate(group.getName(), "httpd.conf", "httpd.conf content for testing");
        assertNotNull(response);
    }

    @Test
    public void testPreviewWebServerTemplate() {
        Response response = groupServiceRest.previewGroupWebServerResourceTemplate(group.getName(), "httpd.conf");
        assertNotNull(response);
    }

    @Test
    public void testPreviewWebServerTemplateThrowsRuntimeException() {
        when(mockGroupService.previewGroupWebServerResourceTemplate(anyString(), anyString(), any(ResourceGroup.class))).thenThrow(new RuntimeException());
        Response response = groupServiceRest.previewGroupWebServerResourceTemplate(group.getName(), "httpd.conf");
        assertNotNull(response);
        assertTrue(response.getStatus() > 499);
    }

    @Test
    public void testGetWebServerTemplate() {
        Response response = groupServiceRest.getGroupWebServerResourceTemplate(group.getName(), "httpd.conf", false);
        assertNotNull(response);
    }

    @Test
    public void testGenerateAndDeployWebServerFiles() {
        Set<WebServer> emptyWsSet = new HashSet<>();
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(emptyWsSet);
        Response response = groupServiceRest.generateAndDeployGroupWebServersFile(group.getName(), "httpd.conf", mockAuthenticatedUser);
        assertNotNull(response);
    }

    @Test
    public void testGetWebserverResourceNames() {
        Response response = groupServiceRest.getGroupWebServersResourceNames(group.getName());
        assertNotNull(response);
    }

    @Test
    public void testGetJvmResourceNames() {
        Response response = groupServiceRest.getGroupJvmsResourceNames(group.getName());
        assertNotNull(response);
    }

    @Test
    public void testUpdateJvmTemplate() {
        Group mockGroupWithJvms = mock(Group.class);
        Set<Jvm> mockJvms = new HashSet<>();
        mockJvms.add(mockJvm);

        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithJvms);
        when(mockGroupWithJvms.getJvms()).thenReturn(mockJvms);

        Response response = groupServiceRest.updateGroupJvmResourceTemplate(group.getName(), "server.xml", "test server.xml content");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        when(mockGroupService.updateGroupJvmResourceTemplate(anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("test jvm", "server.xml"));

        response = groupServiceRest.updateGroupJvmResourceTemplate(group.getName(), "server.xml", "server.xml content for testing");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

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
        Response response = groupServiceRest.uploadGroupJvmConfigTemplate(group.getName(), mockAuthenticatedUser, "server.xml");
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
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroup.getWebServers()).thenReturn(emptyWsSet);
        Response response = groupServiceRest.generateAndDeployGroupJvmFile(group.getName(), "server.xml", mockAuthenticatedUser);
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
        when(mockResourceService.getResourceTypes()).thenReturn(resourceTypesList);
        Response response = groupServiceRest.populateGroupJvmTemplates(group.getName(), mockAuthenticatedUser);
        assertNotNull(response);
    }

    @Test(expected = InternalErrorException.class)
    public void testPopulateGroupJvmTemplateThrowsExceptionForFileNotFound() {
        Collection<ResourceType> resourceTypesList = new ArrayList<>();
        ResourceType mockResourceType = mock(ResourceType.class);
        when(mockResourceType.getEntityType()).thenReturn("jvm");
        when(mockResourceType.getConfigFileName()).thenReturn("server.xml");
        when(mockResourceType.getTemplateName()).thenReturn("ServerXMLTemplate_FILE-NOT-FOUND.tpl");
        resourceTypesList.add(mockResourceType);
        when(mockResourceService.getResourceTypes()).thenReturn(resourceTypesList);
        groupServiceRest.populateGroupJvmTemplates(group.getName(), mockAuthenticatedUser);
    }

    @Test
    public void testPopulateGroupWebServerTemplate() {
        Collection<ResourceType> resourceTypesList = new ArrayList<>();
        ResourceType mockResourceType = mock(ResourceType.class);
        when(mockResourceType.getEntityType()).thenReturn("webServer");
        when(mockResourceType.getConfigFileName()).thenReturn("httpd.conf");
        when(mockResourceType.getTemplateName()).thenReturn("HttpdSslConfTemplate.tpl");
        resourceTypesList.add(mockResourceType);
        when(mockResourceService.getResourceTypes()).thenReturn(resourceTypesList);
        Response response = groupServiceRest.populateGroupWebServerTemplates(group.getName(), mockAuthenticatedUser);
        assertNotNull(response);
    }

    @Test(expected = InternalErrorException.class)
    public void testPopulateGroupWebServerTemplateThrowsExceptionForFileNotFound() {
        Collection<ResourceType> resourceTypesList = new ArrayList<>();
        ResourceType mockResourceType = mock(ResourceType.class);
        when(mockResourceType.getEntityType()).thenReturn("webServer");
        when(mockResourceType.getConfigFileName()).thenReturn("httpd.conf");
        when(mockResourceType.getTemplateName()).thenReturn("HttpdSslConfTemplate_FILE-NOT-FOUND.tpl");
        resourceTypesList.add(mockResourceType);
        when(mockResourceService.getResourceTypes()).thenReturn(resourceTypesList);
        groupServiceRest.populateGroupWebServerTemplates(group.getName(), mockAuthenticatedUser);
    }

    @Test
    public void testUpdateGroupWebServerTemplateNoWebServers() {
        Group mockGroupNoWebServers = mock(Group.class);
        when(mockGroupNoWebServers.getWebServers()).thenReturn(null);
        when(mockGroupService.updateGroupWebServerResourceTemplate(anyString(), anyString(), anyString())).thenReturn("no content updated");
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupNoWebServers);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroupNoWebServers);
        Response response = groupServiceRest.updateGroupWebServerResourceTemplate("groupName", "resourceTemplateName.txt", "no content");
        assertTrue(response.getStatus() > 199 && response.getStatus() < 300);
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
        verify(mockGroupService).populateGroupWebServerTemplates(anyString(), anyList(), any(User.class));
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
        verify(mockGroupService).populateGroupAppTemplate(anyString(), anyString(), anyString(), anyString());
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
        verify(mockGroupService).populateGroupJvmTemplates(anyString(), anyList(), any(User.class));
    }

    @Test
    public void testTestUpdateGroupJvmTemplateNoJvms() {
        when(mockGroupService.updateGroupJvmResourceTemplate(anyString(), anyString(), anyString())).thenReturn("no content updated");
        Group mockGroupNoJvms = mock(Group.class);
        when(mockGroupNoJvms.getJvms()).thenReturn(null);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupNoJvms);
        Response response = groupServiceRest.updateGroupJvmResourceTemplate("groupName", "resourceTemplateName.xml", "no content");
        assertTrue(response.getStatus() > 199 && response.getStatus() < 300);
    }

    @Test
    public void testPreviewGroupAppResourceTemplate() {
        final ResourceGroup resourceGroup = new ResourceGroup();
        when(mockResourceService.generateResourceGroup()).thenReturn(resourceGroup);
        groupServiceRest.previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!");
        verify(mockGroupService).previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!", resourceGroup);

        when(mockGroupService.previewGroupAppResourceTemplate(anyString(), anyString(), anyString(), any(ResourceGroup.class))).thenThrow(new RuntimeException());
        Response response = groupServiceRest.previewGroupAppResourceTemplate("testGroup", "hct.xml", "preview me!");
        assertTrue(response.getStatus() > 499);
    }

    @Test
    public void testGetGroupAppResourceTemplate() {
        groupServiceRest.getGroupAppResourceTemplate("testGroup", "hct.xml", false);
        verify(mockGroupService).getGroupAppResourceTemplate(eq("testGroup"), eq("hct.xml"), eq(false), any(ResourceGroup.class));
    }

    @Test
    public void testGetGroupAppResourceNames() {
        groupServiceRest.getGroupAppResourceNames("testGroup");
        verify(mockGroupService).getGroupAppsResourceTemplateNames("testGroup");
    }

    @Test
    public void testUploadConfigNoContent() throws IOException {

        verify(mockGroupService, never()).populateGroupJvmTemplates(anyString(), anyList(), any(User.class));

        // ISO8859-1
        String boundary = "--WebKitFormBoundarywBZFyEeqG5xW80nx";

        String content = "";

        String charsetBin = "ISO-8859-1";
        ByteBuffer bbBuffer = Charset.forName(charsetBin).encode(content);
        final HttpServletRequest mockHsr = mock(HttpServletRequest.class);
        final MessageContext msgContextMock = mock(MessageContext.class);
        final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
        final HttpHeaders httpHeadersMock = mock(HttpHeaders.class);
        final List<MediaType> mediaTypeList = new ArrayList<>();
        when(httpHeadersMock.getAcceptableMediaTypes()).thenReturn(mediaTypeList);
        when(msgContextMock.getHttpHeaders()).thenReturn(httpHeadersMock);
        when(msgContextMock.getHttpServletRequest()).thenReturn(mockHsr);
        when(msgContextMock.getHttpServletResponse()).thenReturn(httpServletResponseMock);
        when(mockHsr.getCharacterEncoding()).thenReturn(charsetBin);
        when(mockHsr.getInputStream()).thenReturn(new MyIS(new ByteArrayInputStream(bbBuffer.array())));
        when(mockHsr.getContentType()).thenReturn(FileUploadBase.MULTIPART_FORM_DATA + ";boundary=" + boundary);
        groupServiceRest.setMessageContext(msgContextMock);

        Response resp = groupServiceRest.uploadGroupJvmConfigTemplate(group.getName(), mockAuthenticatedUser, "ServerXMLTemplate.tpl");
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testGetStartedWebServersAndJvmCounts() {
        when(mockGroupService.getGroups()).thenReturn(new ArrayList<>(groups));
        when(mockJvmService.getJvmStartedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmCount(anyString())).thenReturn(1L);
        when(mockWebServerService.getWebServerStartedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerCount(anyString())).thenReturn(1L);

        Response response = groupServiceRest.getStartedWebServersAndJvmsCount();
        assertNotNull(response.getEntity());

        response = groupServiceRest.getStartedWebServersAndJvmsCount("testGroup");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGetStoppedWebServersAndJvmsCount() {
        when(mockGroupService.getGroups()).thenReturn(new ArrayList<>(groups));
        when(mockJvmService.getJvmStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmForciblyStoppedCount(anyString())).thenReturn(0L);
        when(mockJvmService.getJvmCount(anyString())).thenReturn(1L);
        when(mockWebServerService.getWebServerStoppedCount(anyString())).thenReturn(0L);
        when(mockWebServerService.getWebServerCount(anyString())).thenReturn(1L);

        Response response = groupServiceRest.getStoppedWebServersAndJvmsCount();
        assertNotNull(response);

        response = groupServiceRest.getStoppedWebServersAndJvmsCount("testGroup");
        assertNotNull(response);

    }

    @Test
    public void testAreAllJvmsStopped() {
        when(mockGroupService.getGroup(anyString())).thenReturn(group);
        Response response = groupServiceRest.areAllJvmsStopped("testGroup");
        assertNotNull(response);

        Group mockGroupWithJvms = mock(Group.class);
        Set<Jvm> mockJvmList = new HashSet<>();
        Jvm mockJvmStarted = mock(Jvm.class);
        mockJvmList.add(mockJvmStarted);

        when(mockJvmStarted.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockGroupWithJvms.getJvms()).thenReturn(mockJvmList);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithJvms);
        response = groupServiceRest.areAllJvmsStopped("testGroup");
        assertNotNull(response);

        when(mockJvmStarted.getState()).thenReturn(JvmState.JVM_STOPPED);
        response = groupServiceRest.areAllJvmsStopped("testGroup");
        assertNotNull(response);
     }

    @Test
    public void testAreAllWebServerStopped() {
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroup);
        Response response = groupServiceRest.areAllWebServersStopped("testGroup");
        assertNotNull(response);

        Group mockGroupWithWebServers = mock(Group.class);
        Set<WebServer> mockWSList = new HashSet<>();
        WebServer mockWSStarted = mock(WebServer.class);
        mockWSList.add(mockWSStarted);

        when(mockWSStarted.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(mockGroupWithWebServers.getWebServers()).thenReturn(mockWSList);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroupWithWebServers);
        when(mockGroupService.getGroupWithWebServers(any(Identifier.class))).thenReturn(mockGroupWithWebServers);
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        response = groupServiceRest.areAllWebServersStopped("testGroup");
        assertNotNull(response);

        when(mockWSStarted.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(false);
        response = groupServiceRest.areAllWebServersStopped("testGroup");
        assertNotNull(response);
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

    private class MyIS extends ServletInputStream {

        private InputStream backingStream;

        public MyIS(InputStream backingStream) {
            this.backingStream = backingStream;
        }

        @Override
        public int read() throws IOException {
            return backingStream.read();
        }

    }
}
