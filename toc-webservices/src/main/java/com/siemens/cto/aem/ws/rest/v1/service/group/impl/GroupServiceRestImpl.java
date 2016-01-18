package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.common.request.webserver.UploadHttpdConfTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateCommandBuilder;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.GroupIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupChildType;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.group.MembershipDetails;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JvmServiceRestImpl;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

public class GroupServiceRestImpl implements GroupServiceRest {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceRestImpl.class);

    private final GroupService groupService;
    private final ResourceService resourceService;

    @Autowired
    private GroupControlService groupControlService;

    @Autowired
    private GroupJvmControlService groupJvmControlService;

    @Autowired
    private GroupWebServerControlService groupWebServerControlService;

    @Autowired
    @Qualifier("groupStateService")
    private StateService<Group, GroupState> groupStateService;

    public GroupServiceRestImpl(final GroupService theGroupService, ResourceService theResourceService) {
        groupService = theGroupService;
        resourceService = theResourceService;
    }

    @Override
    public Response getGroups(final NameSearchParameterProvider aGroupNameSearch, final boolean fetchWebServers) {
        logger.debug("Get Groups requested with search: {}", aGroupNameSearch.getName());

        final List<Group> groups;
        if (aGroupNameSearch.isNamePresent()) {
            groups = groupService.findGroups(aGroupNameSearch.getName());
        } else {
            groups = groupService.getGroups(fetchWebServers);
        }

        return ResponseBuilder.ok(groups);
    }

    @Override
    public Response getGroup(final String groupIdOrName, final boolean byName) {
        if (byName) {
            return ResponseBuilder.ok(groupService.getGroup(groupIdOrName));
        }
        final Identifier<Group> groupId = new Identifier<Group>(groupIdOrName);
        logger.debug("Get Group requested: {}", groupId);
        return ResponseBuilder.ok(groupService.getGroup(groupId));
    }

    @Override
    public Response createGroup(final String aNewGroupName,
                                final AuthenticatedUser aUser) {
        logger.debug("Create Group requested: {}", aNewGroupName);
        final Group group = groupService.createGroup(new CreateGroupRequest(aNewGroupName),
                aUser.getUser());
        populateGroupJvmTemplates(aNewGroupName, aUser);
        populateGroupWebServerTemplates(aNewGroupName, aUser);
        return ResponseBuilder.created(group);
    }

    @Override

    public Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                                final AuthenticatedUser aUser) {
        logger.debug("Update Group requested: {}", anUpdatedGroup);

        // TODO: Refactor adhoc conversion to process group name instead of Id.
        final Group group = groupService.getGroup(anUpdatedGroup.getId());
        final JsonUpdateGroup updatedGroup = new JsonUpdateGroup(group.getId().getId().toString(),
                anUpdatedGroup.getName());

        return ResponseBuilder.ok(groupService.updateGroup(updatedGroup.toUpdateGroupCommand(),
                aUser.getUser()));
    }

    @Override
    public Response removeGroup(final String name, final boolean byName) {
        logger.debug("Delete Group requested: {} byName={}", name, byName);
        if (byName) {
            groupService.removeGroup(name);
        } else {
            groupService.removeGroup(new Identifier<Group>(name));
        }
        return ResponseBuilder.ok();
    }

    @Override
    public Response removeJvmFromGroup(final Identifier<Group> aGroupId,
                                       final Identifier<Jvm> aJvmId,
                                       final AuthenticatedUser aUser) {
        logger.debug("Remove JVM from Group requested: {}, {}", aGroupId, aJvmId);
        return ResponseBuilder.ok(groupService.removeJvmFromGroup(new RemoveJvmFromGroupRequest(aGroupId,
                        aJvmId),
                aUser.getUser()));
    }

    @Override
    public Response addJvmsToGroup(final Identifier<Group> aGroupId,
                                   final JsonJvms someJvmsToAdd,
                                   final AuthenticatedUser aUser) {
        logger.debug("Add JVM to Group requested: {}, {}", aGroupId, someJvmsToAdd);
        final AddJvmsToGroupRequest command = someJvmsToAdd.toCommand(aGroupId);
        return ResponseBuilder.ok(groupService.addJvmsToGroup(command,
                aUser.getUser()));
    }

    @Override
    public Response controlGroupJvms(final Identifier<Group> aGroupId,
                                     final JsonControlJvm jsonControlJvm,
                                     final AuthenticatedUser aUser) {
        logger.debug("Control all JVMs in Group requested: {}, {}", aGroupId, jsonControlJvm);
        final JvmControlOperation command = jsonControlJvm.toControlOperation();
        final ControlGroupJvmRequest grpCommand = new ControlGroupJvmRequest(aGroupId,
                JvmControlOperation.convertFrom(command.getExternalValue()));
        groupJvmControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response populateJvmConfig(final Identifier<Group> aGroupId, final AuthenticatedUser aUser, final boolean overwriteExisting) {
        List<UploadJvmTemplateRequest> uploadJvmTemplateCommands = new ArrayList<>();
        for (Jvm jvm : groupService.getGroup(aGroupId).getJvms()) {
            for (final ResourceType resourceType : resourceService.getResourceTypes()) {
                if ("jvm".equals(resourceType.getEntityType()) && !"invoke.bat".equals(resourceType.getConfigFileName())) {
                    FileInputStream dataInputStream;
                    try {
                        dataInputStream = new FileInputStream(new File(ApplicationProperties.get("paths.resource-types") + "/" + resourceType.getTemplateName()));
                        UploadJvmTemplateRequest uploadJvmTemplateCommand = new UploadJvmTemplateRequest(jvm, resourceType.getTemplateName(), dataInputStream) {
                            @Override
                            public String getConfFileName() {
                                return resourceType.getConfigFileName();
                            }
                        };
                        uploadJvmTemplateCommands.add(uploadJvmTemplateCommand);
                    } catch (FileNotFoundException e) {
                        throw new InternalErrorException(AemFaultType.INVALID_PATH, "Could not find resource template", e);
                    }
                }
            }
        }
        return ResponseBuilder.ok(groupService.populateJvmConfig(aGroupId, uploadJvmTemplateCommands, aUser.getUser(), overwriteExisting));
    }

    @Override
    public Response populateWebServerConfig(final Identifier<Group> aGroupId, final AuthenticatedUser aUser, final boolean overwriteExisting) {
        List<UploadWebServerTemplateRequest> uploadWSTemplateCommands = new ArrayList<>();
        UploadWebServerTemplateCommandBuilder uploadCommandBuilder = new UploadWebServerTemplateCommandBuilder();
        for (WebServer webServer : groupService.getGroupWithWebServers(aGroupId).getWebServers()) {
            UploadHttpdConfTemplateRequest httpdConfTemplateCommand = uploadCommandBuilder.buildHttpdConfCommand(webServer);
            uploadWSTemplateCommands.add(httpdConfTemplateCommand);
        }
        return ResponseBuilder.ok(groupService.populateWebServerConfig(aGroupId, uploadWSTemplateCommands, aUser.getUser(), overwriteExisting));
    }

    @Override
    public Response uploadGroupWebServerConfigTemplate(String groupName, AuthenticatedUser aUser, String templateName) {
        final boolean doGroupWebServerUpload = false;
        return uploadConfigTemplate(groupName, aUser, templateName, doGroupWebServerUpload);
    }

    @Override
    public Response updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content) {
        try {
            return ResponseBuilder.ok(groupService.updateGroupWebServerResourceTemplate(groupName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            logger.debug("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }

    }

    @Override
    public Response previewGroupWebServerResourceTemplate(String groupName, String template) {
        return ResponseBuilder.ok(groupService.previewGroupWebServerResourceTemplate(groupName, template));
    }

    @Override
    public Response getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced) {
        return ResponseBuilder.ok(groupService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName, tokensReplaced));
    }

    @Override
    public Response populateGroupJvmTemplates(String groupName, AuthenticatedUser aUser) {
        List<UploadJvmTemplateRequest> uploadJvmTemplateCommands = new ArrayList<>();
        for (final ResourceType resourceType : resourceService.getResourceTypes()) {
            final String configFileName = resourceType.getConfigFileName();
            if ("jvm".equals(resourceType.getEntityType()) && !"invoke.bat".equals(configFileName)) {
                FileInputStream dataInputStream;
                try {
                    final String templateName = resourceType.getTemplateName();
                    dataInputStream = new FileInputStream(new File(ApplicationProperties.get("paths.resource-types") + "/" + templateName));
                    final Jvm dummyJvm = new Jvm(new Identifier<Jvm>(0L), configFileName, new HashSet<Group>());
                    UploadJvmTemplateRequest uploadJvmTemplateCommand = new UploadJvmTemplateRequest(dummyJvm, templateName, dataInputStream) {
                        @Override
                        public String getConfFileName() {
                            return configFileName;
                        }
                    };
                    uploadJvmTemplateCommands.add(uploadJvmTemplateCommand);
                } catch (FileNotFoundException e) {
                    throw new InternalErrorException(AemFaultType.INVALID_PATH, "Could not find resource template", e);
                }
            }
        }
        return ResponseBuilder.ok(groupService.populateGroupJvmTemplates(groupName, uploadJvmTemplateCommands, aUser.getUser()));
    }

    @Override
    public Response generateAndDeployGroupJvmFile(String groupName, String fileName, AuthenticatedUser aUser) {
        Group group = groupService.getGroup(groupName);
        String groupJvmTemplateContent = groupService.getGroupJvmResourceTemplate(groupName, fileName, false);
        JvmServiceRest jvmServiceRest = JvmServiceRestImpl.get();
        for (Jvm jvm : group.getJvms()) {
            String jvmName = jvm.getJvmName();
            jvmServiceRest.updateResourceTemplate(jvmName, fileName, groupJvmTemplateContent);
            jvmServiceRest.generateAndDeployFile(jvmName, fileName, aUser);
        }
        return ResponseBuilder.ok(group);
    }

    @Override
    public Response getGroupJvmResourceTemplate(final String groupName,
                                                final String resourceTemplateName,
                                                final boolean tokensReplaced) {
        return ResponseBuilder.ok(groupService.getGroupJvmResourceTemplate(groupName, resourceTemplateName, tokensReplaced));
    }

    @Override
    public Response uploadGroupJvmConfigTemplate(String groupName, AuthenticatedUser aUser, String templateName) {
        final boolean doGroupJvmUpload = true;
        return uploadConfigTemplate(groupName, aUser, templateName, doGroupJvmUpload);
    }

    @Override
    public Response updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content) {
        try {
            return ResponseBuilder.ok(groupService.updateGroupJvmResourceTemplate(groupName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            logger.debug("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }

    }

    @Override
    public Response previewGroupJvmResourceTemplate(String groupName, String template) {
        return ResponseBuilder.ok(groupService.previewGroupJvmResourceTemplate(groupName, template));
    }


    @Override
    public Response populateGroupWebServerTemplates(String groupName, AuthenticatedUser aUser) {
        List<UploadWebServerTemplateRequest> uploadWebServerTemplateRequests = new ArrayList<>();
        for (final ResourceType resourceType : resourceService.getResourceTypes()) {
            final String configFileName = resourceType.getConfigFileName();
            if ("webServer".equals(resourceType.getEntityType())) {
                FileInputStream dataInputStream;
                try {
                    final String templateName = resourceType.getTemplateName();
                    dataInputStream = new FileInputStream(new File(ApplicationProperties.get("paths.resource-types") + "/" + templateName));
                    final WebServer dummyWebServer = new WebServer(new Identifier<WebServer>(0L), new HashSet<Group>(), "");
                    UploadWebServerTemplateRequest uploadWSTemplateRequest = new UploadWebServerTemplateRequest(dummyWebServer, templateName, dataInputStream) {
                        @Override
                        public String getConfFileName() {
                            return configFileName;
                        }
                    };
                    uploadWebServerTemplateRequests.add(uploadWSTemplateRequest);
                } catch (FileNotFoundException e) {
                    throw new InternalErrorException(AemFaultType.INVALID_PATH, "Could not find resource template", e);
                }
            }
        }
        return ResponseBuilder.ok(groupService.populateGroupWebServerTemplates(groupName, uploadWebServerTemplateRequests, aUser.getUser()));
    }

    @Override
    public Response generateAndDeployGroupWebServersFile(String groupName, AuthenticatedUser aUser) {
        Group group = groupService.getGroup(groupName);
        group = groupService.getGroupWithWebServers(group.getId());
        String httpdTemplateContent = groupService.getGroupWebServerResourceTemplate(groupName, "httpd.conf", false);
        WebServerServiceRestImpl webServerServiceRest = WebServerServiceRestImpl.get();
        for (WebServer webserver : group.getWebServers()) {
            webServerServiceRest.updateResourceTemplate(webserver.getName(), "httpd.conf", httpdTemplateContent);
            webServerServiceRest.generateAndDeployConfig(webserver.getName());
        }
        return ResponseBuilder.ok(group);
    }

    @Override
    public Response controlGroupWebservers(final Identifier<Group> aGroupId,
                                           final JsonControlWebServer jsonControlWebServer,
                                           final AuthenticatedUser aUser) {
        logger.debug("Control all WebServers in Group requested: {}, {}", aGroupId, jsonControlWebServer);
        final WebServerControlOperation command = jsonControlWebServer.toControlOperation();
        final ControlGroupWebServerRequest grpCommand = new ControlGroupWebServerRequest(aGroupId,
                WebServerControlOperation.convertFrom(command.getExternalValue()));
        groupWebServerControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlGroup(final Identifier<Group> aGroupId,
                                 final JsonControlGroup jsonControlGroup,
                                 final AuthenticatedUser aUser) {

        GroupControlOperation groupControlOperation = jsonControlGroup.toControlOperation();
        logger.debug("starting control group {} with operation {}", aGroupId, groupControlOperation);

        ControlGroupRequest grpCommand = new ControlGroupRequest(aGroupId, groupControlOperation);
        groupControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response resetState(final Identifier<Group> aGroupId,
                               final AuthenticatedUser aUser) {
        return ResponseBuilder.ok(groupControlService.resetState(aGroupId,
                aUser.getUser()));
    }

    @Override
    public Response getCurrentJvmStates(final GroupIdsParameterProvider aGroupIdsParameterProvider) {
        logger.debug("Current Group states requested : {}", aGroupIdsParameterProvider);
        final Set<Identifier<Group>> groupIds = aGroupIdsParameterProvider.valueOf();
        final Set<CurrentState<Group, GroupState>> currentGroupStates;

        if (groupIds.isEmpty()) {
            currentGroupStates = groupStateService.getCurrentStates();
        } else {
            currentGroupStates = groupStateService.getCurrentStates(groupIds);
        }

        return ResponseBuilder.ok(currentGroupStates);
    }

    private List<MembershipDetails> createMembershipDetailsFromJvms(final List<Jvm> jvms) {
        final List<MembershipDetails> membershipDetailsList = new LinkedList<>();
        for (Jvm jvm : jvms) {
            final List<String> groupNames = new LinkedList<>();
            for (Group group : jvm.getGroups()) {
                groupNames.add(group.getName());
            }
            membershipDetailsList.add(new MembershipDetails(jvm.getJvmName(),
                    GroupChildType.JVM,
                    groupNames));
        }
        return membershipDetailsList;
    }

    private List<MembershipDetails> createMembershipDetailsFromWebServers(final List<WebServer> webServers) {
        final List<MembershipDetails> membershipDetailsList = new LinkedList<>();
        for (WebServer webServer : webServers) {
            final List<String> groupNames = new LinkedList<>();
            for (Group group : webServer.getGroups()) {
                groupNames.add(group.getName());
            }
            membershipDetailsList.add(new MembershipDetails(webServer.getName(),
                    GroupChildType.WEB_SERVER,
                    groupNames));
        }
        return membershipDetailsList;
    }

    @Override
    public Response getOtherGroupMembershipDetailsOfTheChildren(final Identifier<Group> id,
                                                                final GroupChildType groupChildType) {
        final List<Jvm> jvmGroupingDetails;
        final List<WebServer> webServerGroupingDetails;

        if (groupChildType != null) {
            if (groupChildType == GroupChildType.JVM) {
                jvmGroupingDetails = groupService.getOtherGroupingDetailsOfJvms(id);
                return ResponseBuilder.ok(createMembershipDetailsFromJvms(jvmGroupingDetails));
            } else if (groupChildType == GroupChildType.WEB_SERVER) {
                webServerGroupingDetails = groupService.getOtherGroupingDetailsOfWebServers(id);
                return ResponseBuilder.ok(createMembershipDetailsFromWebServers(webServerGroupingDetails));
            }
        }

        jvmGroupingDetails = groupService.getOtherGroupingDetailsOfJvms(id);
        final List<MembershipDetails> membershipDetailsList = createMembershipDetailsFromJvms(jvmGroupingDetails);
        webServerGroupingDetails = groupService.getOtherGroupingDetailsOfWebServers(id);
        membershipDetailsList.addAll(createMembershipDetailsFromWebServers(webServerGroupingDetails));

        return ResponseBuilder.ok(membershipDetailsList);
    }

    @Override
    public Response getGroupJvmsResourceNames(String groupName) {
        return ResponseBuilder.ok(groupService.getGroupJvmsResourceTemplateNames(groupName));
    }

    @Override
    public Response getGroupWebServersResourceNames(String groupName) {
        return ResponseBuilder.ok(groupService.getGroupWebServersResourceTemplateNames(groupName));
    }

    @Context
    private MessageContext context;

    // FOR UNIT TEST ONLY
    public void setMessageContext(MessageContext testContext){
        context = testContext;
    }

    private Response uploadConfigTemplate(final String groupName, final AuthenticatedUser aUser, final String templateName, final boolean isGroupJvmUpload) {
        logger.debug("Upload Archive requested: {} streaming (no size, count yet)", groupName);

        // iframe uploads from IE do not understand application/json
        // as a response and will prompt for download. Fix: return
        // text/html
        if (!context.getHttpHeaders().getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            context.getHttpServletResponse().setContentType(MediaType.TEXT_HTML);
        }

        ServletFileUpload sfu = new ServletFileUpload();
        InputStream data = null;
        try {
            FileItemIterator iter = sfu.getItemIterator(context.getHttpServletRequest());
            FileItemStream file1;

            while (iter.hasNext()) {
                file1 = iter.next();
                try {
                    data = file1.openStream();
                    if (isGroupJvmUpload) {
                        return doGroupJvmTemplateUpload(groupName, aUser, templateName, data, file1);
                    } else {
                        return doGroupWebServerTemplateUpload(groupName, aUser, templateName, data, file1);
                    }
                } finally {
                    assert data != null;
                    data.close();
                }
            }
            return ResponseBuilder.notOk(Response.Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_JVM_OPERATION, "No data"));
        } catch (IOException | FileUploadException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    private Response doGroupWebServerTemplateUpload(String groupName, AuthenticatedUser aUser, final String templateName, final InputStream data, FileItemStream file1) {
        final WebServer dummyWebServer = new WebServer(new Identifier<WebServer>(0L), new HashSet<Group>(), "");
        UploadWebServerTemplateRequest uploadWSTemplateRequest = new UploadWebServerTemplateRequest(dummyWebServer, file1.getName(), data) {
            @Override
            public String getConfFileName() {
                return templateName;
            }
        };
        List<UploadWebServerTemplateRequest> uploadWSTemplateCommands = new ArrayList<>();
        uploadWSTemplateCommands.add(uploadWSTemplateRequest);
        return ResponseBuilder.created(groupService.populateGroupWebServerTemplates(groupName, uploadWSTemplateCommands, aUser.getUser()));
    }

    private Response doGroupJvmTemplateUpload(String groupName, AuthenticatedUser aUser, final String templateName, final InputStream data, final FileItemStream file1) {
        Jvm dummyJvm = new Jvm(new Identifier<Jvm>(0L), "", new HashSet());
        UploadJvmTemplateRequest uploadJvmTemplateRequest = new UploadJvmTemplateRequest(dummyJvm, file1.getName(), data) {
            @Override
            public String getConfFileName() {
                return templateName;
            }
        };

        final ArrayList<UploadJvmTemplateRequest> uploadJvmTemplateCommands = new ArrayList<>();
        uploadJvmTemplateCommands.add(uploadJvmTemplateRequest);
        return ResponseBuilder.created(groupService.populateGroupJvmTemplates(groupName, uploadJvmTemplateCommands, aUser.getUser()));
    }

}
