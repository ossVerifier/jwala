package com.cerner.jwala.ws.rest.v1.service.group.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupControlOperation;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.*;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.group.GroupControlService;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.provider.NameSearchParameterProvider;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import com.cerner.jwala.ws.rest.v1.service.app.impl.ApplicationServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.group.GroupChildType;
import com.cerner.jwala.ws.rest.v1.service.group.GroupServiceRest;
import com.cerner.jwala.ws.rest.v1.service.group.MembershipDetails;
import com.cerner.jwala.ws.rest.v1.service.jvm.JvmServiceRest;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JvmServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

public class GroupServiceRestImpl implements GroupServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceRestImpl.class);

    private final GroupService groupService;
    private final ResourceService resourceService;
    private final ExecutorService executorService;
    private final ApplicationService applicationService;
    private GroupControlService groupControlService;
    private GroupJvmControlService groupJvmControlService;
    private GroupWebServerControlService groupWebServerControlService;
    private final JvmService jvmService;
    private final WebServerService webServerService;

    @Autowired
    public GroupServiceRestImpl(final GroupService groupService, final ResourceService resourceService,
                                final GroupControlService groupControlService, final GroupJvmControlService groupJvmControlService,
                                final GroupWebServerControlService groupWebServerControlService, final JvmService jvmService,
                                final WebServerService webServerService, ApplicationService applicationService) {
        this.groupService = groupService;
        this.resourceService = resourceService;
        this.groupControlService = groupControlService;
        this.groupJvmControlService = groupJvmControlService;
        this.groupWebServerControlService = groupWebServerControlService;
        this.jvmService = jvmService;
        this.webServerService = webServerService;
        this.applicationService = applicationService;
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
        LOGGER.debug("Get Group requested: {} byName={}", groupIdOrName, byName);
        if (byName) {
            return ResponseBuilder.ok(groupService.getGroup(groupIdOrName));
        }
        final Identifier<Group> groupId = new Identifier<>(groupIdOrName);
        return ResponseBuilder.ok(groupService.getGroup(groupId));
    }

    @Override
    public Response createGroup(final String aNewGroupName,
                                final AuthenticatedUser aUser) {
        try {
            LOGGER.info("Create Group requested: {} by user {}", aNewGroupName, aUser.getUser().getId());
            final Group group = groupService.createGroup(new CreateGroupRequest(aNewGroupName), aUser.getUser());
            return ResponseBuilder.created(group);
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.DUPLICATE_GROUP_NAME, eee.getMessage(), eee));
        }
    }


    @Override

    public Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                                final AuthenticatedUser aUser) {
        LOGGER.info("Update Group requested: {} by user {}", anUpdatedGroup, aUser.getUser().getId());
        try {
            // TODO: Refactor adhoc conversion to process group name instead of Id.
            final Group group = groupService.getGroup(anUpdatedGroup.getId());
            final JsonUpdateGroup updatedGroup = new JsonUpdateGroup(group.getId().getId().toString(),
                    anUpdatedGroup.getName());

            return ResponseBuilder.ok(groupService.updateGroup(updatedGroup.toUpdateGroupCommand(),
                    aUser.getUser()));
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.DUPLICATE_GROUP_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response removeGroup(final String name, final boolean byName) {
        LOGGER.info("Delete Group requested: {} byName={}", name, byName);
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
        LOGGER.info("Remove JVM from Group requested: {}, {} by user {}", aGroupId, aJvmId, aUser.getUser().getId());
        return ResponseBuilder.ok(groupService.removeJvmFromGroup(new RemoveJvmFromGroupRequest(aGroupId,
                        aJvmId),
                aUser.getUser()));
    }

    @Override
    public Response addJvmsToGroup(final Identifier<Group> aGroupId,
                                   final JsonJvms someJvmsToAdd,
                                   final AuthenticatedUser aUser) {
        LOGGER.info("Add JVM to Group requested: {}, {} by user {}", aGroupId, someJvmsToAdd, aUser.getUser().getId());
        final AddJvmsToGroupRequest command = someJvmsToAdd.toCommand(aGroupId);
        return ResponseBuilder.ok(groupService.addJvmsToGroup(command,
                aUser.getUser()));
    }

    @Override
    public Response controlGroupJvms(final Identifier<Group> aGroupId,
                                     final JsonControlJvm jsonControlJvm,
                                     final AuthenticatedUser aUser) {
        LOGGER.info("Control all JVMs in Group requested: {}, {} by user {}", aGroupId, jsonControlJvm, aUser.getUser().getId());
        final JvmControlOperation command = jsonControlJvm.toControlOperation();
        final ControlGroupJvmRequest grpCommand = new ControlGroupJvmRequest(aGroupId,
                JvmControlOperation.convertFrom(command.getExternalValue()));
        groupJvmControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response uploadGroupWebServerConfigTemplate(String groupName, AuthenticatedUser aUser, String templateName) {
        LOGGER.info("Uploading group web server template {} to {} by user {}", templateName, groupName, aUser.getUser().getId());
        return uploadConfigTemplate(groupName, null, aUser, templateName, GroupResourceType.WEBSERVER);
    }

    @Override
    public Response updateGroupWebServerResourceTemplate(final String groupName, final String resourceTemplateName, final String content) {
        LOGGER.info("update group web server resource template {} for group {}", resourceTemplateName, groupName);
        try {
            final String updatedContent = groupService.updateGroupWebServerResourceTemplate(groupName, resourceTemplateName, content);
            Group group = groupService.getGroup(groupName);
            group = groupService.getGroupWithWebServers(group.getId());

            Set<WebServer> groupWebServers = group.getWebServers();
            Set<Future<Response>> futureContents = new HashSet<>();
            if (null != groupWebServers) {
                LOGGER.info("Updating the templates for all the Web Servers in group {}", groupName);
                for (final WebServer webServer : groupWebServers) {
                    final String webServerName = webServer.getName();
                    LOGGER.info("Updating Web Server {} template {}", webServerName, resourceTemplateName);
                    Future<Response> futureContent = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            return ResponseBuilder.ok(webServerService.updateResourceTemplate(webServerName, resourceTemplateName, updatedContent));
                        }
                    });
                    futureContents.add(futureContent);
                }
                waitForDeployToComplete(futureContents);
            } else {
                LOGGER.info("No Web Servers to update in group {}", groupName);
            }

            LOGGER.info("Update SUCCESSFUL");
            return ResponseBuilder.ok(updatedContent);

        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response previewGroupWebServerResourceTemplate(String groupName, String template) {
        LOGGER.debug("Preview group web server template for group {}", groupName);
        LOGGER.debug(template);
        try {
            return ResponseBuilder.ok(groupService.previewGroupWebServerResourceTemplate(groupName, template, resourceService.generateResourceGroup()));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to preview the web server template for {}", groupName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, e.getMessage(), e));
        }
    }

    @Override
    public Response getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced) {
        LOGGER.debug("Get group web server resource template {} for group {} : tokens replaced={}", resourceTemplateName, groupName, tokensReplaced);
        return ResponseBuilder.ok(groupService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName, tokensReplaced, tokensReplaced ? resourceService.generateResourceGroup() : new ResourceGroup()));
    }

    @Override
    public Response generateAndDeployGroupJvmFile(final String groupName, final String fileName, final AuthenticatedUser aUser) {
        LOGGER.info("generate and deploy group JVM file {} to group {} by user {}", fileName, groupName, aUser.getUser().getId());
        Group group = groupService.getGroup(groupName);
        final boolean doNotReplaceTokens = false;
        final String groupJvmTemplateContent = groupService.getGroupJvmResourceTemplate(groupName, fileName, resourceService.generateResourceGroup(), doNotReplaceTokens);
        Set<Future<Response>> futures = new HashSet<>();
        final JvmServiceRest jvmServiceRest = JvmServiceRestImpl.get();
        final Set<Jvm> jvms = group.getJvms();
        if (null != jvms && jvms.size() > 0) {
            for (final Jvm jvm : jvms) {
                if (jvm.getState().isStartedState()) {
                    LOGGER.info("Failed to deploy file {} for group {}: not all JVMs were stopped - {} was started", fileName, group.getName(), jvm.getJvmName());
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "All JVMs in the group must be stopped before continuing. Operation stopped for JVM " + jvm.getJvmName());
                }
            }
            for (final Jvm jvm : jvms) {
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
        } else {
            LOGGER.info("No JVMs in group {}", groupName);
        }
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
                for (Future<Response> isDoneFuture : futures) {
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
        LOGGER.debug("Get group JVM resource template {} for group {} : tokens replaced={}", resourceTemplateName, groupName, tokensReplaced);
        return ResponseBuilder.ok(groupService.getGroupJvmResourceTemplate(groupName, resourceTemplateName, resourceService.generateResourceGroup(), tokensReplaced));
    }

    @Override
    public Response uploadGroupJvmConfigTemplate(String groupName, AuthenticatedUser aUser, String templateName) {
        LOGGER.info("upload group jvm template {} to group {} by user {}", templateName, groupName, aUser.getUser().getId());
        return uploadConfigTemplate(groupName, null, aUser, templateName, GroupResourceType.JVM);
    }

    @Override
    public Response updateGroupJvmResourceTemplate(final String groupName, final String resourceTemplateName, final String content) {
        LOGGER.info("Updating the group template {} for {}", resourceTemplateName, groupName);
        LOGGER.debug(content);

        try {

            final String updatedContent = groupService.updateGroupJvmResourceTemplate(groupName, resourceTemplateName, content);
            final Group group = groupService.getGroup(groupName);

            Set<Jvm> groupJvms = group.getJvms();
            Set<Future<Response>> futureContents = new HashSet<>();
            if (null != groupJvms) {
                LOGGER.info("Updating the templates for all the JVMs in group {}", groupName);
                for (final Jvm jvm : groupJvms) {
                    final String jvmName = jvm.getJvmName();
                    LOGGER.info("Updating JVM {} template {}", jvmName, resourceTemplateName);
                    Future<Response> futureContent = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            return ResponseBuilder.ok(jvmService.updateResourceTemplate(jvmName, resourceTemplateName, updatedContent));
                        }
                    });
                    futureContents.add(futureContent);
                }
                waitForDeployToComplete(futureContents);
            } else {
                LOGGER.info("No JVMs to update in group {}", groupName);
            }

            LOGGER.info("Update SUCCESSFUL");
            return ResponseBuilder.ok(updatedContent);

        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response previewGroupJvmResourceTemplate(String groupName, String template) {
        LOGGER.debug("Preview group JVM resource template for group {}", groupName);
        LOGGER.debug(template);
        try {
            final Group group = groupService.getGroup(groupName);
            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            final Set<Jvm> jvms = group.getJvms();
            return ResponseBuilder.ok(resourceService.generateResourceFile(template, resourceGroup, (null != jvms && jvms.size() > 0 ? jvms.iterator().next() : null)));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to preview the JVM template for {}", groupName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, e.getMessage(), e));
        }
    }

    @Override
    public Response generateAndDeployGroupWebServersFile(final String groupName, final String resourceFileName, final AuthenticatedUser aUser) {
        LOGGER.info("generate and deploy the web server file {} to group {} by user", resourceFileName, groupName, aUser.getUser().getId());
        Group group = groupService.getGroup(groupName);
        group = groupService.getGroupWithWebServers(group.getId());
        final String httpdTemplateContent = groupService.getGroupWebServerResourceTemplate(groupName, resourceFileName, false, resourceService.generateResourceGroup());
        final WebServerServiceRestImpl webServerServiceRest = WebServerServiceRestImpl.get();
        final Set<WebServer> webServers = group.getWebServers();
        if (null != webServers && webServers.size() > 0) {
            for (WebServer webServer : webServers) {
                if (webServerService.isStarted(webServer)) {
                    LOGGER.info("Failed to deploy {} for group {}: not all web servers were stopped - {} was started",
                            resourceFileName, group.getName(), webServer.getName());
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                            "All web servers in the group must be stopped before continuing. Operation stopped for web server "
                                    + webServer.getName());
                }
            }

            final Map<String, Future<Response>> futureMap = new HashMap<>();
            for (final WebServer webserver : webServers) {
                final String name = webserver.getName();
                Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                    @Override
                    public Response call() throws Exception {
                        webServerServiceRest.updateResourceTemplate(name, resourceFileName, httpdTemplateContent);
                        return webServerServiceRest.generateAndDeployConfig(name, resourceFileName, aUser);

                    }
                });
                futureMap.put(name, responseFuture);
            }
            waitForDeployToComplete(new HashSet<>(futureMap.values()));
            checkResponsesForErrorStatus(futureMap);
        } else {
            LOGGER.info("No web servers in group {}", groupName);
        }
        return ResponseBuilder.ok(httpdTemplateContent);
    }

    protected void checkResponsesForErrorStatus(Map<String, Future<Response>> futureMap) {
        for (String keyEntityName : futureMap.keySet()) {
            Response response;
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
        LOGGER.debug("Control all WebServers in Group requested: {}, {} by user {}", aGroupId, jsonControlWebServer, aUser.getUser().getId());
        final WebServerControlOperation command = jsonControlWebServer.toControlOperation();
        final ControlGroupWebServerRequest grpCommand = new ControlGroupWebServerRequest(aGroupId,
                WebServerControlOperation.convertFrom(command.getExternalValue()));
        groupWebServerControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response generateGroupWebservers(Identifier<Group> aGroupId, final AuthenticatedUser aUser) {
        LOGGER.info("Starting group generation of web servers for group ID {} by user {}", aGroupId, aUser.getUser().getId());
        try {
            Group group = groupService.getGroupWithWebServers(aGroupId);
            Set<WebServer> webServers = group.getWebServers();
            if (null != webServers && webServers.size() > 0) {
                for (WebServer webServer : webServers) {
                    if (webServerService.isStarted(webServer)) {
                        LOGGER.info("Failed to start generation of web servers for group ID {}: not all web servers were stopped - {} was started", aGroupId, webServer.getName());
                        throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "All web servers in the group must be stopped before continuing. Operation stopped for web server " + webServer.getName());
                    }
                }

                // generate and deploy the web servers
                final WebServerServiceRestImpl webServerServiceRest = WebServerServiceRestImpl.get();
                Map<String, Future<Response>> futuresMap = new HashMap<>();
                for (final WebServer webServer : webServers) {
                    final String webServerName = webServer.getName();
                    Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            return webServerServiceRest.generateAndDeployWebServer(webServerName, aUser);
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
        } catch (InternalErrorException iee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_WEBSERVER_OPERATION, iee.getMessage(), iee));
        }
    }

    @Override
    public Response generateGroupJvms(final Identifier<Group> aGroupId, final AuthenticatedUser aUser) {
        LOGGER.info("Starting group generation of JVMs for group ID {} by user {}", aGroupId, aUser.getUser().getId());
        try {
            final Group group = groupService.getGroup(aGroupId);
            Set<Jvm> jvms = group.getJvms();
            if (null != jvms && jvms.size() > 0) {
                for (Jvm jvm : jvms) {
                    if (jvm.getState().isStartedState()) {
                        LOGGER.info("Failed to start generation of JVMs for group ID {}: not all JVMs were stopped - {} was started", aGroupId, jvm.getJvmName());
                        throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "All JVMs in the group must be stopped before continuing. Operation stopped for JVM " + jvm.getJvmName());
                    }
                    jvmService.checkForSetenvBat(jvm.getJvmName());
                }

                final JvmServiceRest jvmServiceRest = JvmServiceRestImpl.get();

                // generate and deploy the JVMs
                Map<String, Future<Response>> futuresMap = new HashMap<>();
                for (final Jvm jvm : jvms) {
                    final String jvmName = jvm.getJvmName();
                    Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            return jvmServiceRest.generateAndDeployJvm(jvmName, aUser);
                        }
                    });
                    futuresMap.put(jvmName, responseFuture);
                }

                waitForDeployToComplete(new HashSet<>(futuresMap.values()));
                checkResponsesForErrorStatus(futuresMap);
            } else {
                LOGGER.info("No JVMs in group {}", aGroupId);
            }

            return ResponseBuilder.ok(group);
        } catch (InternalErrorException iee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_WEBSERVER_OPERATION, iee.getMessage(), iee));
        }
    }

    @Override
    public Response controlGroup(final Identifier<Group> aGroupId,
                                 final JsonControlGroup jsonControlGroup,
                                 final AuthenticatedUser aUser) {

        GroupControlOperation groupControlOperation = jsonControlGroup.toControlOperation();
        LOGGER.info("Starting control group {} with operation {} by user {}", aGroupId, groupControlOperation, aUser.getUser().getId());

        ControlGroupRequest grpCommand = new ControlGroupRequest(aGroupId, groupControlOperation);
        groupControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlGroups(final JsonControlGroup jsonControlGroup, final AuthenticatedUser authenticatedUser) {
        LOGGER.info("Control groups {} by user {}", jsonControlGroup, authenticatedUser.getUser().getId());
        groupControlService.controlGroups(new ControlGroupRequest(null, jsonControlGroup.toControlOperation()),
                authenticatedUser.getUser());
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

        LOGGER.debug("Get other group membership details of the children for group {} and child {}", id, groupChildType);
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
        LOGGER.debug("Get group JVMs resource names for group {}", groupName);
        return ResponseBuilder.ok(groupService.getGroupJvmsResourceTemplateNames(groupName));
    }

    @Override
    public Response getGroupWebServersResourceNames(String groupName) {
        LOGGER.debug("Get group web server resource names for group {}", groupName);
        return ResponseBuilder.ok(groupService.getGroupWebServersResourceTemplateNames(groupName));
    }

    @Context
    private MessageContext context;

    // FOR UNIT TEST ONLY
    public void setMessageContext(MessageContext testContext) {
        context = testContext;
    }

    protected Response uploadConfigTemplate(final String groupName, final String targetEntityName, final AuthenticatedUser aUser,
                                            final String templateName, final GroupResourceType uploadType) {
        LOGGER.debug("Upload Archive {} requested: {} streaming (no size, count yet) : target {} upload type {} by user {}", templateName, groupName, targetEntityName, uploadType, aUser.getUser().getId());

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
                        return doGroupAppTemplateUpload(groupName, targetEntityName, templateName, data);
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
        Scanner scanner = new Scanner(data).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        UploadWebServerTemplateRequest uploadWSTemplateRequest = new UploadWebServerTemplateRequest(dummyWebServer, file1.getName(), templateContent) {
            @Override
            public String getConfFileName() {
                return templateName;
            }
        };
        Map<String, UploadWebServerTemplateRequest> uploadWSTemplateCommands = new HashMap<>();
        uploadWSTemplateCommands.put(templateName, uploadWSTemplateRequest);
        return ResponseBuilder.created(groupService.populateGroupWebServerTemplates(groupName, uploadWSTemplateCommands, aUser.getUser()));
    }

    protected Response doGroupJvmTemplateUpload(String groupName, AuthenticatedUser aUser, final String templateName, final InputStream data, final FileItemStream file1) {
        Jvm dummyJvm = new Jvm(new Identifier<Jvm>(0L), "", new HashSet());
        Scanner scanner = new Scanner(data).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        UploadJvmTemplateRequest uploadJvmTemplateRequest = new UploadJvmTemplateRequest(dummyJvm, file1.getName(), templateContent) {
            @Override
            public String getConfFileName() {
                return templateName;
            }
        };

        final ArrayList<UploadJvmTemplateRequest> uploadJvmTemplateCommands = new ArrayList<>();
        uploadJvmTemplateCommands.add(uploadJvmTemplateRequest);
        return ResponseBuilder.created(groupService.populateGroupJvmTemplates(groupName, uploadJvmTemplateCommands, aUser.getUser()));
    }

    protected Response doGroupAppTemplateUpload(final String groupName, final String appName, final String templateName,
                                                final InputStream data) {
        Scanner scanner = new Scanner(data).useDelimiter("\\A");
        String content = scanner.hasNext() ? scanner.next() : "";

        // meta data can be empty since the method below (I assume based on the usage) updates an existing template rather
        // than creating a new one.
        return ResponseBuilder.created(groupService.populateGroupAppTemplate(groupName, appName, templateName,
                StringUtils.EMPTY, content));
    }

    @Override
    public Response updateGroupAppResourceTemplate(final String groupName, final String appName, final String resourceTemplateName, final String content) {

        LOGGER.info("Updating the group template {} for {}", resourceTemplateName, groupName);
        LOGGER.debug(content);

        String metaDataStr = groupService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplateName);
        final Group group = groupService.getGroup(groupName);

        try {
            final String updatedContent = groupService.updateGroupAppResourceTemplate(groupName, appName, resourceTemplateName, content);
            ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);

            Set<Jvm> groupJvms = group.getJvms();
            Set<Future<Response>> futureContents = new HashSet<>();
            if (null != groupJvms) {
                LOGGER.info("Updating the templates for all the JVMs in group {}", groupName);
                final ApplicationServiceRest appServiceRest = ApplicationServiceRestImpl.get();
                for (final Jvm jvm : groupJvms) {
                    final String jvmName = jvm.getJvmName();
                    LOGGER.info("Updating JVM {} template {}", jvmName, resourceTemplateName);
                    Future<Response> futureContent = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            return appServiceRest.updateResourceTemplate(appName, resourceTemplateName, jvmName, groupName, updatedContent);
                        }
                    });
                    futureContents.add(futureContent);
                }
                waitForDeployToComplete(futureContents);
            } else {
                LOGGER.info("No JVMs to update in group {}", groupName);
            }

            LOGGER.info("Update SUCCESSFUL");
            return ResponseBuilder.ok(updatedContent);

        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        } catch (JsonMappingException | JsonParseException e) {
            LOGGER.error("Failed to map meta data object for template {} in group {} :: meta data: {} ", resourceTemplateName, groupName, metaDataStr, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.BAD_STREAM, e.getMessage()));
        } catch (IOException e) {
            LOGGER.error("Failed with IOException trying to map meta data object for template {} in group {} :: meta data: {}", resourceTemplateName, groupName, metaDataStr, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.BAD_STREAM, e.getMessage()));
        }

    }

    @Override
    public Response generateAndDeployGroupAppFile(final String groupName, final String fileName, final AuthenticatedUser aUser, final String hostName) {

        LOGGER.info("Generate and deploy group app file {} for group {} by user {} to host {}", fileName, groupName, aUser.getUser().getId(), hostName);

        Group group = groupService.getGroup(groupName);
        final String groupAppMetaData = groupService.getGroupAppResourceTemplateMetaData(groupName, fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        ResourceTemplateMetaData metaData;
        try {
            metaData = objectMapper.readValue(groupAppMetaData, ResourceTemplateMetaData.class);
            final String appName = metaData.getEntity().getTarget();
            final ApplicationServiceRest appServiceRest = ApplicationServiceRestImpl.get();
            if (metaData.getEntity().getDeployToJvms()) {
                // deploy to all jvms in group
                performGroupAppDeployToJvms(groupName, fileName, aUser, group, appName, appServiceRest, hostName);
            } else if (hostName != null && !hostName.isEmpty()) {
                // deploy to particular host
                performGroupAppDeployToHost(groupName, fileName, appName, hostName);
            } else {
                // deploy to all hosts in group
                performGroupAppDeployToHosts(groupName, fileName, appName);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to map meta data for resource template {} in group {} :: meta data: {} ", fileName, groupName, groupAppMetaData, e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Failed to map meta data for resource template " + fileName + " in group " + groupName, e);
        }
        return ResponseBuilder.ok(group);
    }

    /**
     * This method deploys group app config template to only one host
     *
     * @param groupName name of the group we can find the webapp under
     * @param fileName  name of the file that needs to be deployed to the host
     * @param appName   name of the application which needs to be deployed
     * @param hostName  name of the host to which we want the file to be deployed to
     */
    protected void performGroupAppDeployToHost(final String groupName, final String fileName, final String appName, final String hostName) {
        Map<String, Future<Response>> futureMap = new HashMap<>();
        futureMap.put(hostName, createFutureResponseForAppDeploy(groupName, fileName, appName, null, hostName));
        waitForDeployToComplete(new HashSet<>(futureMap.values()));
        checkResponsesForErrorStatus(futureMap);
    }

    protected void performGroupAppDeployToHosts(final String groupName, final String fileName, final String appName) {
        Map<String, Future<Response>> futureMap = new HashMap<>();
        Set<Jvm> jvms = groupService.getGroup(groupName).getJvms();
        if (null != jvms && jvms.size() > 0) {
            for (Jvm jvm : jvms) {
                if (jvm.getState().isStartedState()) {
                    LOGGER.info("Failed to deploy file {} for group {}: not all JVMs were stopped - {} was started", fileName, groupName, jvm.getJvmName());
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "All JVMs in the group must be stopped before continuing. Operation stopped for JVM " + jvm.getJvmName());
                }
            }
            List<String> deployedHosts = new LinkedList<>();
            for (final Jvm jvm : jvms) {
                final String hostName = jvm.getHostName();
                if (!deployedHosts.contains(hostName)) {
                    deployedHosts.add(hostName);
                    futureMap.put(hostName, createFutureResponseForAppDeploy(groupName, fileName, appName, jvm, null));
                }
            }
            waitForDeployToComplete(new HashSet<>(futureMap.values()));
            checkResponsesForErrorStatus(futureMap);
        }
    }

    protected void performGroupAppDeployToJvms(final String groupName, final String fileName, final AuthenticatedUser aUser, final Group group,
                                               final String appName, final ApplicationServiceRest appServiceRest, final String hostName) {
        Map<String, Future<Response>> futureMap = new HashMap<>();
        final Set<Jvm> groupJvms = group.getJvms();
        Set<Jvm> jvms = null;
        if (hostName != null && !hostName.isEmpty()) {
            LOGGER.debug("got hostname {} deploying template to host jvms only", hostName);
            jvms = new HashSet<>();
            for (Jvm jvm : groupJvms) {
                if (jvm.getHostName().equalsIgnoreCase(hostName)) {
                    jvms.add(jvm);
                }
            }
        } else {
            LOGGER.debug("got no hostname deploying to all group jvms");
            jvms = groupJvms;
        }
        if (null != jvms && jvms.size() > 0) {
            for (Jvm jvm : jvms) {
                if (jvm.getState().isStartedState()) {
                    LOGGER.info("Failed to deploy file {} for group {}: not all JVMs were stopped - {} was started", fileName, group.getName(), jvm.getJvmName());
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "All JVMs in the group must be stopped before continuing. Operation stopped for JVM " + jvm.getJvmName());
                }
            }
            final String groupAppTemplateContent = groupService.getGroupAppResourceTemplate(groupName, appName, fileName, false, new ResourceGroup());
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
        }
    }

    /**
     * Creates future object for application template deploy.
     *
     * @param groupName name of the group under which the app exists
     * @param fileName  name of the file that needs to be deployed
     * @param appName   name of the app which contains the template
     * @param jvm       name of the jvm to which the app needs to deploy the template to
     * @param hostName  name of the host where the resources need to be deployed to
     * @return returns a Future<Response> object if successful.
     */
    protected Future<Response> createFutureResponseForAppDeploy(final String groupName, final String fileName, final String appName, final Jvm jvm, final String hostName) {
        final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
        final Application application = applicationService.getApplication(appName);
        Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                CommandOutput commandOutput = null;
                if (jvm != null) {
                    LOGGER.debug("got jvm object with id {}, creating command output with jvm", jvm.getId().getId());
                    commandOutput = groupService.deployGroupAppTemplate(groupName, fileName, resourceGroup, application, jvm);
                } else {
                    LOGGER.debug("got jvm as null creating app templates for hostname {}", hostName);
                    commandOutput = groupService.deployGroupAppTemplate(groupName, fileName, resourceGroup, application, hostName);
                }
                if (commandOutput.getReturnCode().wasSuccessful()) {
                    return ResponseBuilder.ok(commandOutput);
                } else {
                    return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(AemFaultType.REMOTE_COMMAND_FAILURE, commandOutput.toString()));
                }
            }
        });
        return responseFuture;
    }

    @Override
    public Response uploadGroupAppConfigTemplate(final String groupName, final String appName, final AuthenticatedUser aUser,
                                                 final String templateName) {
        LOGGER.info("Upload group app config template {} for app {} in group {} by user {}", templateName, appName, groupName, aUser.getUser().getId());
        return uploadConfigTemplate(groupName, appName, aUser, templateName, GroupResourceType.WEBAPP);
    }

    @Override
    public Response previewGroupAppResourceTemplate(String groupName, String resourceTemplateName, String template) {
        LOGGER.debug("Preview group app resource {} in group {}", resourceTemplateName, groupName);
        LOGGER.debug(template);
        try {
            return ResponseBuilder.ok(groupService.previewGroupAppResourceTemplate(groupName, resourceTemplateName, template, resourceService.generateResourceGroup()));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to preview the application template {} for {}", resourceTemplateName, groupName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, e.getMessage(), e));
        }
    }

    @Override
    public Response getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, boolean tokensReplaced) {
        LOGGER.debug("Get group app resource template {} for app {} in group {} : tokens replaced={}", resourceTemplateName, appName, groupName, tokensReplaced);
        return ResponseBuilder.ok(groupService.getGroupAppResourceTemplate(groupName, appName, resourceTemplateName, tokensReplaced, tokensReplaced ? resourceService.generateResourceGroup() : new ResourceGroup()));
    }

    @Override
    public Response getGroupAppResourceNames(String groupName) {
        LOGGER.debug("Get group app resource names {}", groupName);
        return ResponseBuilder.ok(groupService.getGroupAppsResourceTemplateNames(groupName));
    }

    @Override
    public Response getStartedWebServersAndJvmsCount() {
        LOGGER.debug("Get started Web Servers and JVMs count");
        final List<GroupServerInfo> groupServerInfos = new ArrayList<>();
        for (final Group group : groupService.getGroups()) {
            final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(group.getName())
                    .setJvmStartedCount(jvmService.getJvmStartedCount(group.getName()))
                    .setJvmCount(jvmService.getJvmCount(group.getName()))
                    .setWebServerStartedCount(webServerService.getWebServerStartedCount(group.getName()))
                    .setWebServerCount(webServerService.getWebServerCount(group.getName())).build();
            groupServerInfos.add(groupServerInfo);
        }
        return ResponseBuilder.ok(groupServerInfos);
    }

    @Override
    public Response getStartedAndStoppedWebServersAndJvmsCount() {
        LOGGER.debug("Get started and stopped Web Servers and JVMs count");
        final List<GroupServerInfo> groupServerInfos = new ArrayList<>();
        for (final Group group : groupService.getGroups()) {
            final GroupServerInfo groupServerInfo = getGroupServerInfo(group.getName());
            groupServerInfos.add(groupServerInfo);
        }
        return ResponseBuilder.ok(groupServerInfos);
    }

    @Override
    public Response getStartedWebServersAndJvmsCount(final String groupName) {
        LOGGER.debug("Get started Web Servers and JVMs count for group {}", groupName);
        final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(groupName)
                .setJvmStartedCount(jvmService.getJvmStartedCount(groupName))
                .setJvmCount(jvmService.getJvmCount(groupName))
                .setWebServerStartedCount(webServerService.getWebServerStartedCount(groupName))
                .setWebServerCount(webServerService.getWebServerCount(groupName)).build();
        return ResponseBuilder.ok(groupServerInfo);
    }

    @Override
    public Response getStartedAndStoppedWebServersAndJvmsCount(final String groupName) {
        LOGGER.debug("Get started and stopped Web Servers and JVMs coount in group {}", groupName);
        final GroupServerInfo groupServerInfo = getGroupServerInfo(groupName);
        return ResponseBuilder.ok(groupServerInfo);
    }

    /**
     * Get a group's children servers info (e.g. jvm count, web server count etc...)
     *
     * @param groupName the group name
     * @return {@GroupServerInfo}
     */
    protected GroupServerInfo getGroupServerInfo(final String groupName) {
        return new GroupServerInfoBuilder().setGroupName(groupName)
                .setJvmStartedCount(jvmService.getJvmStartedCount(groupName))
                .setJvmStoppedCount(jvmService.getJvmStoppedCount(groupName))
                .setJvmForciblyStoppedCount(jvmService.getJvmForciblyStoppedCount(groupName))
                .setJvmCount(jvmService.getJvmCount(groupName))
                .setWebServerStartedCount(webServerService.getWebServerStartedCount(groupName))
                .setWebServerStoppedCount(webServerService.getWebServerStoppedCount(groupName))
                .setWebServerCount(webServerService.getWebServerCount(groupName)).build();
    }

    @Override
    public Response getStoppedWebServersAndJvmsCount() {
        LOGGER.debug("Get stopped Web Servers and JVMs count");
        final List<GroupServerInfo> groupServerInfos = new ArrayList<>();
        for (final Group group : groupService.getGroups()) {
            final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(group.getName())
                    .setJvmStoppedCount(jvmService.getJvmStoppedCount(group.getName()))
                    .setJvmForciblyStoppedCount(jvmService.getJvmForciblyStoppedCount(group.getName()))
                    .setJvmCount(jvmService.getJvmCount(group.getName()))
                    .setWebServerStoppedCount(webServerService.getWebServerStoppedCount(group.getName()))
                    .setWebServerCount(webServerService.getWebServerCount(group.getName())).build();
            groupServerInfos.add(groupServerInfo);
        }
        return ResponseBuilder.ok(groupServerInfos);
    }

    @Override
    public Response getStoppedWebServersAndJvmsCount(final String groupName) {
        LOGGER.debug("Get stopped Web Servers and JVMs count in group {}", groupName);
        final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(groupName)
                .setJvmStoppedCount(jvmService.getJvmStoppedCount(groupName))
                .setJvmForciblyStoppedCount(jvmService.getJvmForciblyStoppedCount(groupName))
                .setJvmCount(jvmService.getJvmCount(groupName))
                .setWebServerStoppedCount(webServerService.getWebServerStoppedCount(groupName))
                .setWebServerCount(webServerService.getWebServerCount(groupName)).build();
        return ResponseBuilder.ok(groupServerInfo);
    }

    @Override
    public Response areAllJvmsStopped(final String groupName) {
        LOGGER.debug("Are all JVMs stopped in group {}", groupName);
        HashMap<String, String> resultTrue = new HashMap<>();
        resultTrue.put("allStopped", Boolean.TRUE.toString());
        Group group = groupService.getGroup(groupName);
        Set<Jvm> jvms = group.getJvms();
        if (null != jvms && jvms.size() > 0) {
            for (final Jvm jvm : jvms) {
                if (jvm.getState().isStartedState()) {
                    HashMap<String, String> notStopped = new HashMap<>();
                    notStopped.put("allStopped", Boolean.FALSE.toString());
                    notStopped.put("entityNotStopped", jvm.getJvmName());
                    return ResponseBuilder.ok(notStopped);
                }
            }
            return ResponseBuilder.ok(resultTrue);
        } else {
            return ResponseBuilder.ok(resultTrue);
        }
    }

    @Override
    public Response areAllWebServersStopped(final String groupName) {
        LOGGER.debug("Are all web servers stopped in group {}", groupName);
        HashMap<String, String> resultTrue = new HashMap<>();
        resultTrue.put("allStopped", Boolean.TRUE.toString());
        Group group = groupService.getGroup(groupName);
        group = groupService.getGroupWithWebServers(group.getId());
        Set<WebServer> webServers = group.getWebServers();
        if (null != webServers && webServers.size() > 0) {
            for (final WebServer webServer : webServers) {
                if (webServerService.isStarted(webServer)) {
                    HashMap<String, String> notStopped = new HashMap<>();
                    notStopped.put("allStopped", Boolean.FALSE.toString());
                    notStopped.put("entityNotStopped", webServer.getName());
                    return ResponseBuilder.ok(notStopped);
                }
            }
            return ResponseBuilder.ok(resultTrue);
        } else {
            return ResponseBuilder.ok(resultTrue);
        }
    }

    @Override
    public Response getHosts(final String groupName) {
        return ResponseBuilder.ok(groupService.getHosts(groupName));
    }

    @Override
    public Response getAllHosts() {
        return ResponseBuilder.ok(groupService.getAllHosts());
    }
}
