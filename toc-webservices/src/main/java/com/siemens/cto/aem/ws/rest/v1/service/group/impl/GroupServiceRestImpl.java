package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
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
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.app.impl.ApplicationServiceRestImpl;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupChildType;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.group.MembershipDetails;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JvmServiceRestImpl;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.WebServerServiceRest;
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

import javax.ws.rs.BeanParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class GroupServiceRestImpl implements GroupServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceRestImpl.class);

    private final GroupService groupService;
    private final ResourceService resourceService;
    private final ExecutorService executorService;
    private GroupControlService groupControlService;
    private GroupJvmControlService groupJvmControlService;
    private GroupWebServerControlService groupWebServerControlService;
    private final JvmService jvmService;
    private final WebServerService webServerService;

    @Autowired
    public GroupServiceRestImpl(final GroupService groupService, final ResourceService resourceService,
                                final GroupControlService groupControlService, final GroupJvmControlService groupJvmControlService,
                                final GroupWebServerControlService groupWebServerControlService, final JvmService jvmService,
                                final WebServerService webServerService) {
        this.groupService = groupService;
        this.resourceService = resourceService;
        this.groupControlService = groupControlService;
        this.groupJvmControlService = groupJvmControlService;
        this.groupWebServerControlService = groupWebServerControlService;
        this.jvmService = jvmService;
        this.webServerService = webServerService;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("resources.thread-task-executor.pool.size", "25")));
    }

    @Override
    public Response getGroups(final NameSearchParameterProvider aGroupNameSearch, final boolean fetchWebServers) {
        LOGGER.debug("Get Groups requested with search: {}", aGroupNameSearch.getName());

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
        LOGGER.debug("Get Group requested: {}", groupId);
        return ResponseBuilder.ok(groupService.getGroup(groupId));
    }

    @Override
    public Response createGroup(final String aNewGroupName,
                                final AuthenticatedUser aUser) {
        LOGGER.debug("Create Group requested: {}", aNewGroupName);
        final Group group = groupService.createGroup(new CreateGroupRequest(aNewGroupName), aUser.getUser());
        populateGroupJvmTemplates(aNewGroupName, aUser);
        populateGroupWebServerTemplates(aNewGroupName, aUser);
        return ResponseBuilder.created(group);
    }

    @Override

    public Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                                final AuthenticatedUser aUser) {
        LOGGER.debug("Update Group requested: {}", anUpdatedGroup);

        // TODO: Refactor adhoc conversion to process group name instead of Id.
        final Group group = groupService.getGroup(anUpdatedGroup.getId());
        final JsonUpdateGroup updatedGroup = new JsonUpdateGroup(group.getId().getId().toString(),
                anUpdatedGroup.getName());

        return ResponseBuilder.ok(groupService.updateGroup(updatedGroup.toUpdateGroupCommand(),
                aUser.getUser()));
    }

    @Override
    public Response removeGroup(final String name, final boolean byName) {
        LOGGER.debug("Delete Group requested: {} byName={}", name, byName);
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
        LOGGER.debug("Remove JVM from Group requested: {}, {}", aGroupId, aJvmId);
        return ResponseBuilder.ok(groupService.removeJvmFromGroup(new RemoveJvmFromGroupRequest(aGroupId,
                        aJvmId),
                aUser.getUser()));
    }

    @Override
    public Response addJvmsToGroup(final Identifier<Group> aGroupId,
                                   final JsonJvms someJvmsToAdd,
                                   final AuthenticatedUser aUser) {
        LOGGER.debug("Add JVM to Group requested: {}, {}", aGroupId, someJvmsToAdd);
        final AddJvmsToGroupRequest command = someJvmsToAdd.toCommand(aGroupId);
        return ResponseBuilder.ok(groupService.addJvmsToGroup(command,
                aUser.getUser()));
    }

    @Override
    public Response controlGroupJvms(final Identifier<Group> aGroupId,
                                     final JsonControlJvm jsonControlJvm,
                                     final AuthenticatedUser aUser) {
        LOGGER.debug("Control all JVMs in Group requested: {}, {}", aGroupId, jsonControlJvm);
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
                        LOGGER.error("Invalid Path: Could not find resource template", e);
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
        return uploadConfigTemplate(groupName, aUser, templateName, GroupResourceType.WEBSERVER);
    }

    @Override
    public Response updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content) {
        try {
            return ResponseBuilder.ok(groupService.updateGroupWebServerResourceTemplate(groupName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.debug("Failed to update the template {}", resourceTemplateName, e);
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
                    LOGGER.error("Invalid Path: Could not find resource template", e);
                    throw new InternalErrorException(AemFaultType.INVALID_PATH, "Could not find resource template", e);
                }
            }
        }
        return ResponseBuilder.ok(groupService.populateGroupJvmTemplates(groupName, uploadJvmTemplateCommands, aUser.getUser()));
    }

    @Override
    public Response generateAndDeployGroupJvmFile(final String groupName, final String fileName, final AuthenticatedUser aUser) {
        Group group = groupService.getGroup(groupName);
        final boolean doNotReplaceTokens = false;
        final String groupJvmTemplateContent = groupService.getGroupJvmResourceTemplate(groupName, fileName, doNotReplaceTokens);
        Set<Future<Response>> futures = new HashSet<>();
        final JvmServiceRest jvmServiceRest = JvmServiceRestImpl.get();
        for (final Jvm jvm : group.getJvms()) {
            final String jvmName = jvm.getJvmName();
            Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    jvmServiceRest.updateResourceTemplate(jvmName, fileName, groupJvmTemplateContent);
                    return jvmServiceRest.generateAndDeployFile(jvmName, fileName, aUser);

                }
            });
            futures.add(responseFuture);
        }
        waitForDeployToComplete(futures);
        return ResponseBuilder.ok(group);
    }

    protected void waitForDeployToComplete(Set<Future<Response>> futures) {
        final int size = futures.size();
        if (size > 0) {
            LOGGER.info("Check to see if all {} tasks completed", size);
            boolean allDone = false;
            // TODO think about adding a manual timeout
            while (!allDone) {
                boolean isDone = true;
                for (Future isDoneFuture : futures) {
                    isDone = isDone && isDoneFuture.isDone();
                }
                allDone = isDone;
            }
            LOGGER.info("Tasks complete: {}", size);
        }
    }

    @Override
    public Response getGroupJvmResourceTemplate(final String groupName,
                                                final String resourceTemplateName,
                                                final boolean tokensReplaced) {
        return ResponseBuilder.ok(groupService.getGroupJvmResourceTemplate(groupName, resourceTemplateName, tokensReplaced));
    }

    @Override
    public Response uploadGroupJvmConfigTemplate(String groupName, AuthenticatedUser aUser, String templateName) {
        return uploadConfigTemplate(groupName, aUser, templateName, GroupResourceType.JVM);
    }

    @Override
    public Response updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content) {
        try {
            return ResponseBuilder.ok(groupService.updateGroupJvmResourceTemplate(groupName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.debug("Failed to update the template {}", resourceTemplateName, e);
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
                    LOGGER.error("Invalid Path: Could not find resource template", e);
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
        final String httpdTemplateContent = groupService.getGroupWebServerResourceTemplate(groupName, "httpd.conf", false);
        final WebServerServiceRestImpl webServerServiceRest = WebServerServiceRestImpl.get();
        Map<String, Future<Response>> futureMap = new HashMap<>();
        for (final WebServer webserver : group.getWebServers()) {
            final String name = webserver.getName();
            Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    webServerServiceRest.updateResourceTemplate(name, "httpd.conf", httpdTemplateContent);
                    final boolean doBackup = true;
                    return webServerServiceRest.generateAndDeployConfig(name, doBackup);

                }
            });
            futureMap.put(name, responseFuture);
        }
        waitForDeployToComplete(new HashSet<>(futureMap.values()));
        checkResponsesForErrorStatus(futureMap);
        return ResponseBuilder.ok(group);
    }

    protected void checkResponsesForErrorStatus(Map<String, Future<Response>> futureMap) {
        for (String keyEntityName : futureMap.keySet()) {
            Response response = null;
            try {
                response = futureMap.get(keyEntityName).get();
                if (response.getStatus() > 399) {
                    final String reasonPhrase = response.getStatusInfo().getReasonPhrase();
                    LOGGER.error("Remote Command Failure for " + keyEntityName + ": " + reasonPhrase);
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, reasonPhrase);
                }

            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("FAILURE getting response for {}", keyEntityName, e);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, e.getMessage());
            }
        }
    }

    @Override
    public Response controlGroupWebservers(final Identifier<Group> aGroupId,
                                           final JsonControlWebServer jsonControlWebServer,
                                           final AuthenticatedUser aUser) {
        LOGGER.debug("Control all WebServers in Group requested: {}, {}", aGroupId, jsonControlWebServer);
        final WebServerControlOperation command = jsonControlWebServer.toControlOperation();
        final ControlGroupWebServerRequest grpCommand = new ControlGroupWebServerRequest(aGroupId,
                WebServerControlOperation.convertFrom(command.getExternalValue()));
        groupWebServerControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response generateGroupWebservers(Identifier<Group> aGroupId, final AuthenticatedUser aUser) {
        LOGGER.info("Starting group generation of web servers for group ID {}", aGroupId);
        Group group = groupService.getGroupWithWebServers(aGroupId);
        Set<WebServer> webServers = group.getWebServers();
        if (null != webServers && webServers.size() > 0) {
            for (WebServer webServer : webServers) {
                if (webServerService.isStarted(webServer)) {
                    LOGGER.info("Failed to start generation of web servers for group ID {}: not all web servers were stopped - {} was started", aGroupId, webServer.getName());
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "All web servers in the group must be stopped before continuing. Operation stopped for web server " + webServer.getName());
                }
            }

            final WebServerServiceRestImpl webServerServiceRest = WebServerServiceRestImpl.get();
            Map<String, Future<Response>> futuresMap = new HashMap<>();
            for (final WebServer webServer : webServers) {
                final String webServerName = webServer.getName();
                Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                    @Override
                    public Response call() throws Exception {
                        final boolean doBackup = true;
                        return webServerServiceRest.generateAndDeployWebServer(webServerName, doBackup, aUser);
                    }
                });
                futuresMap.put(webServerName, responseFuture);
            }

            waitForDeployToComplete(new HashSet<>(futuresMap.values()));
            checkResponsesForErrorStatus(futuresMap);
        } else {
            LOGGER.info("No web servers in group {}", aGroupId);
        }
        return ResponseBuilder.ok(group);
    }

    @Override
    public Response generateGroupJvms(final Identifier<Group> aGroupId, final AuthenticatedUser aUser) {
        LOGGER.info("Starting group generation of JVMs for group ID {}", aGroupId);
        Group group = groupService.getGroup(aGroupId);
        Set<Jvm> jvms = group.getJvms();
        if (null != jvms && jvms.size() > 0) {
            for (Jvm jvm : jvms) {
                if (jvm.getState().isStartedState()) {
                    LOGGER.info("Failed to start generation of JVMs for group ID {}: not all JVMs were stopped - {} was started", aGroupId, jvm.getJvmName());
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "All JVMs in the group must be stopped before continuing. Operation stopped for JVM " + jvm.getJvmName());
                }
            }

            final JvmServiceRest jvmServiceRest = JvmServiceRestImpl.get();
            Map<String, Future<Response>> futuresMap = new HashMap<>();
            for (final Jvm jvm : jvms) {
                final String jvmName = jvm.getJvmName();
                Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                    @Override
                    public Response call() throws Exception {
                        return jvmServiceRest.generateAndDeployConf(jvmName, aUser);
                    }
                });
                futuresMap.put(jvmName, responseFuture);
            }

            waitForDeployToComplete(new HashSet<>(futuresMap.values()));
            checkResponsesForErrorStatus(futuresMap);
        } else {
            LOGGER.info("No web servers in group {}", aGroupId);
        }

        return ResponseBuilder.ok(group);
    }

    @Override
    public Response controlGroup(final Identifier<Group> aGroupId,
                                 final JsonControlGroup jsonControlGroup,
                                 final AuthenticatedUser aUser) {

        GroupControlOperation groupControlOperation = jsonControlGroup.toControlOperation();
        LOGGER.debug("starting control group {} with operation {}", aGroupId, groupControlOperation);

        ControlGroupRequest grpCommand = new ControlGroupRequest(aGroupId, groupControlOperation);
        groupControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    protected List<MembershipDetails> createMembershipDetailsFromJvms(final List<Jvm> jvms) {
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

    protected List<MembershipDetails> createMembershipDetailsFromWebServers(final List<WebServer> webServers) {
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
    public Response getGroupJvmsResourceNames(String groupName, boolean includeGroupAppResources) {
        return ResponseBuilder.ok(groupService.getGroupJvmsResourceTemplateNames(groupName, includeGroupAppResources));
    }

    @Override
    public Response getGroupWebServersResourceNames(String groupName) {
        return ResponseBuilder.ok(groupService.getGroupWebServersResourceTemplateNames(groupName));
    }

    @Context
    private MessageContext context;

    // FOR UNIT TEST ONLY
    public void setMessageContext(MessageContext testContext) {
        context = testContext;
    }

    protected Response uploadConfigTemplate(final String groupName, final AuthenticatedUser aUser, final String templateName, final GroupResourceType uploadType) {
        LOGGER.debug("Upload Archive requested: {} streaming (no size, count yet)", groupName);

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
                    if (uploadType.equals(GroupResourceType.JVM)) {
                        return doGroupJvmTemplateUpload(groupName, aUser, templateName, data, file1);
                    } else if (uploadType.equals(GroupResourceType.WEBSERVER)) {
                        return doGroupWebServerTemplateUpload(groupName, aUser, templateName, data, file1);
                    } else if (uploadType.equals(GroupResourceType.WEBAPP)) {
                        return doGroupAppTemplateUpload(groupName, aUser, templateName, data, file1);
                    }
                } finally {
                    assert data != null;
                    data.close();
                }
            }
            LOGGER.info("Failed to upload template {} to group {}: No Data", templateName, groupName);
            return ResponseBuilder.notOk(Response.Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_JVM_OPERATION, "No data"));
        } catch (IOException | FileUploadException e) {
            LOGGER.error("Bad Stream: Error receiving data", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    protected Response doGroupWebServerTemplateUpload(String groupName, AuthenticatedUser aUser, final String templateName, final InputStream data, FileItemStream file1) {
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

    protected Response doGroupJvmTemplateUpload(String groupName, AuthenticatedUser aUser, final String templateName, final InputStream data, final FileItemStream file1) {
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

    protected Response doGroupAppTemplateUpload(String groupName, AuthenticatedUser aUser, final String templateName, final InputStream data, FileItemStream file1) {
        Scanner scanner = new Scanner(data).useDelimiter("\\A");
        String content = scanner.hasNext() ? scanner.next() : "";

        return ResponseBuilder.created(groupService.populateGroupAppTemplate(groupName, templateName, content));
    }

    @Override
    public Response updateGroupAppResourceTemplate(String groupName, String resourceTemplateName, String content) {
        try {
            return ResponseBuilder.ok(groupService.updateGroupAppResourceTemplate(groupName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response generateAndDeployGroupAppFile(final String groupName, final String fileName, final AuthenticatedUser aUser) {
        Group group = groupService.getGroup(groupName);
        final String groupAppTemplateContent = groupService.getGroupAppResourceTemplate(groupName, fileName, false);
        final ApplicationServiceRest appServiceRest = ApplicationServiceRestImpl.get();
        final String appName = groupService.getAppNameFromResourceTemplate(fileName);
        Map<String, Future<Response>> futureMap = new HashMap<>();
        final Set<Jvm> jvms = group.getJvms();
        if (null != jvms && jvms.size() > 0) {
            if (fileName.endsWith(".xml")) {
                for (Jvm jvm : jvms) {
                    final String jvmName = jvm.getJvmName();
                    Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            appServiceRest.updateResourceTemplate(appName, fileName, jvmName, groupName, groupAppTemplateContent);
                            return appServiceRest.deployConf(appName, groupName, jvmName, fileName, aUser);
                        }
                    });
                    futureMap.put(jvmName, responseFuture);
                }
                waitForDeployToComplete(new HashSet<>(futureMap.values()));
                checkResponsesForErrorStatus(futureMap);
            } else {
                final String jvmName = jvms.iterator().next().getJvmName();
                appServiceRest.updateResourceTemplate(appName, fileName, jvmName, groupName, groupAppTemplateContent);
                appServiceRest.deployConf(appName, groupName, jvmName, fileName, aUser);
            }
        }
        return ResponseBuilder.ok(group);
    }

    @Override
    public Response uploadGroupAppConfigTemplate(String groupName, AuthenticatedUser aUser, String templateName) {
        return uploadConfigTemplate(groupName, aUser, templateName, GroupResourceType.WEBAPP);
    }

    @Override
    public Response previewGroupAppResourceTemplate(String groupName, String resourceTemplateName, String template) {
        return ResponseBuilder.ok(groupService.previewGroupAppResourceTemplate(groupName, resourceTemplateName, template));
    }

    @Override
    public Response getGroupAppResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced) {
        return ResponseBuilder.ok(groupService.getGroupAppResourceTemplate(groupName, resourceTemplateName, tokensReplaced));
    }

    @Override
    public Response getGroupAppResourceNames(String groupName) {
        return ResponseBuilder.ok(groupService.getGroupAppsResourceTemplateNames(groupName));
    }

    @Override
    public Response getStartedWebServersAndJvmsCount() {
        final List<GroupServerInfo> groupServerInfos = new ArrayList<>();
        for (final Group group : groupService.getGroups()) {
            final GroupServerInfo groupServerInfo = new GroupServerInfo(group.getName(), jvmService.getJvmCount(group.getName()),
                    jvmService.getJvmStartedCount(group.getName()), webServerService.getWebServerCount(group.getName()),
                    webServerService.getStartedWebServerCount(group.getName()));
            groupServerInfos.add(groupServerInfo);
        }
        return ResponseBuilder.ok(groupServerInfos);
    }

    @Override
    public Response getStartedWebServersAndJvmsCount(final String groupName) {
        final GroupServerInfo groupServerInfo = new GroupServerInfo(groupName, jvmService.getJvmCount(groupName),
                jvmService.getJvmStartedCount(groupName), webServerService.getWebServerCount(groupName),
                webServerService.getStartedWebServerCount(groupName));
        return ResponseBuilder.ok(groupServerInfo);
    }

    public enum GroupResourceType {
        WEBSERVER,
        JVM,
        WEBAPP
    }

    /**
     * Wrapper to pass jvm and web server count information to the UI.
     */
    private static class GroupServerInfo {
        private final String groupName;
        private final Long jvmCount;
        private final Long jvmStartedCount;
        private final Long webServerCount;
        private final Long webServerStartedCount;

        public GroupServerInfo(final String groupName, final Long jvmCount, final Long jvmStartedCount,
                               final Long webServerCount, final Long webServerStartedCount) {
            this.groupName = groupName;
            this.jvmCount = jvmCount;
            this.jvmStartedCount = jvmStartedCount;
            this.webServerCount = webServerCount;
            this.webServerStartedCount = webServerStartedCount;
        }

        public String getGroupName() {
            return groupName;
        }

        public Long getJvmCount() {
            return jvmCount;
        }

        public Long getJvmStartedCount() {
            return jvmStartedCount;
        }

        public Long getWebServerCount() {
            return webServerCount;
        }

        public Long getWebServerStartedCount() {
            return webServerStartedCount;
        }
    }

}
