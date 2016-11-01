package com.cerner.jwala.ws.rest.v1.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.exception.ResourceServiceException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class ApplicationServiceRestImpl implements ApplicationServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceRestImpl.class);

    private ApplicationService service;
    private ResourceService resourceService;
    private ServletFileUpload servletFileUpload;
    private final GroupService groupService;

    private static ApplicationServiceRestImpl instance;

    public ApplicationServiceRestImpl(ApplicationService applicationService, ResourceService resourceService,
                                      ServletFileUpload servletFileUpload, final GroupService groupService) {
        service = applicationService;
        this.resourceService = resourceService;
        this.servletFileUpload = servletFileUpload;
        this.groupService = groupService;
    }

    @Override
    public Response getApplication(Identifier<Application> anAppId) {
        LOGGER.debug("Get App by id: {}", anAppId);
        final Application app = service.getApplication(anAppId);
        return ResponseBuilder.ok(app);
    }

    @Override
    public Response getApplications(Identifier<Group> aGroupId) {
        LOGGER.debug("Get Apps requested with groupId: {}", aGroupId != null ? aGroupId : "null");
        final List<Application> apps;
        if (aGroupId != null) {
            apps = service.findApplications(aGroupId);
        } else {
            apps = service.getApplications();
        }
        return ResponseBuilder.ok(apps);
    }

    @Override
    public Response findApplicationsByJvmId(Identifier<Jvm> aJvmId) {
        LOGGER.debug("Find Apps requested with aJvmId: {}", aJvmId != null ? aJvmId : "null");
        if (aJvmId != null) {
            final List<Application> apps = service.findApplicationsByJvmId(aJvmId);
            return ResponseBuilder.ok(apps);
        } else {
            final List<Application> apps = service.getApplications();
            return ResponseBuilder.ok(apps);
        }
    }

    @Override
    public Response createApplication(final JsonCreateApplication anAppToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Create Application requested: {}", anAppToCreate);
        try {
            Application created = service.createApplication(anAppToCreate.toCreateCommand(), aUser.getUser());
            return ResponseBuilder.created(created);
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.DUPLICATE_APPLICATION, eee.getMessage(), eee));
        }
    }

    @Override
    public Response updateApplication(final JsonUpdateApplication anAppToUpdate, final AuthenticatedUser aUser) {
        LOGGER.info("Update Application requested: {}", anAppToUpdate);
        try {
            Application updated = service.updateApplication(anAppToUpdate.toUpdateCommand(), aUser.getUser());
            return ResponseBuilder.ok(updated);
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.DUPLICATE_APPLICATION, eee.getMessage(), eee));
        }
    }

    @Override
    public Response removeApplication(final Identifier<Application> anAppToRemove, final AuthenticatedUser aUser) {
        LOGGER.info("Delete application requested: {}", anAppToRemove);
        service.removeApplication(anAppToRemove, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Context
    private MessageContext context; // TODO: Define as a method parameter to make this class more testable!

    @Override
    public Response uploadWebArchive(final Identifier<Application> appId, final MessageContext messageContext) {
        InputStream in;
        String deployPath = null;
        String warName = null;
        byte[] war = null;

        try {
            final FileItemIterator it = servletFileUpload.getItemIterator(messageContext.getHttpServletRequest());
            while (it.hasNext()) {
                final FileItemStream fileItemStream = it.next();
                if ("file".equalsIgnoreCase(fileItemStream.getFieldName())) {
                    warName = fileItemStream.getName();
                    in = fileItemStream.openStream();
                    war = IOUtils.toByteArray(in);
                    in.close();
                } else if ("deployPath".equalsIgnoreCase(fileItemStream.getFieldName())) {
                    in = fileItemStream.openStream();
                    deployPath = IOUtils.toString(in);
                    in.close();
                } else {
                    return ResponseBuilder.notOk(Status.INTERNAL_SERVER_ERROR, new FaultCodeException(AemFaultType.INVALID_REST_SERVICE_PARAMETER,
                            "Invalid parameter " + fileItemStream.getFieldName()));
                }
            }

            final Application application = service.uploadWebArchive(appId, warName, war, deployPath);

            // Why created ? Because to upload a new WAR means creating one ? Isn't uploading a new war means
            // "updating an application"
            // Anyways, I just followed the original implementation.
            // TODO: Decide if uploading a new war is a CREATE rather than an UPDATE.
            return ResponseBuilder.created(application);
        } catch (final FileUploadException | ApplicationServiceException | ResourceServiceException | IOException e) {
            LOGGER.error("Error uploading web archive", e);
            return ResponseBuilder.notOk(Status.INTERNAL_SERVER_ERROR, new FaultCodeException(AemFaultType.IO_EXCEPTION,
                    e.getMessage()));
        }
    }

    @Override
    public Response deleteWebArchive(final Identifier<Application> appToRemoveWAR, final AuthenticatedUser aUser) {
        LOGGER.info("Delete Archive requested: {}", appToRemoveWAR);
        Application updated = service.deleteWebArchive(appToRemoveWAR, aUser.getUser());
        return ResponseBuilder.ok(updated);
    }

    @Override
    public Response deployWebArchive(final Identifier<Application> anAppToGet, final AuthenticatedUser aUser) {
        LOGGER.info("Deploying web archive for app ID {}", anAppToGet);
        Application app = service.getApplication(anAppToGet);
        final Group group = app.getGroup();
        Set<Jvm> jvms = group.getJvms();
        final String appName = app.getName();
        final String groupName = group.getName();
        if (null != jvms && jvms.size() > 0) {
            service.copyApplicationWarToGroupHosts(app);
            service.deployApplicationResourcesToGroupHosts(groupName, app, resourceService.generateResourceGroup());
        } else {
            LOGGER.info("Skip deploying application {}, no JVM's in group {}", appName, groupName);
        }

        return ResponseBuilder.ok(app);
    }

    @Override
    public Response deployWebArchive(final Identifier<Application> anAppToGet, String hostName) {
        LOGGER.info("Deploying web archive for app ID {}", anAppToGet);
        Application app = service.getApplication(anAppToGet);
        final Group group = app.getGroup();
        final String appName = app.getName();
        service.copyApplicationWarToHost(app, hostName);
        return null;
    }

    @Override
    public Response getResourceNames(final String appName, final String jvmName) {
        LOGGER.debug("get resource names for {}@{}", appName, jvmName);
        return ResponseBuilder.ok(service.getResourceTemplateNames(appName, jvmName));
    }

    @Override
    public Response getResourceTemplate(final String appName, final String groupName, final String jvmName,
                                        final String resourceTemplateName, final boolean tokensReplaced) {
        LOGGER.debug("get resource template {} for app {} in group {} associated with JVM {} : tokens replaced={}", resourceTemplateName, appName, groupName, jvmName, tokensReplaced);
        return ResponseBuilder.ok(service.getResourceTemplate(appName, groupName, jvmName, resourceTemplateName, resourceService.generateResourceGroup(), tokensReplaced));
    }

    @Override
    public Response updateResourceTemplate(final String appName,
                                           final String resourceTemplateName,
                                           final String jvmName,
                                           final String groupName,
                                           final String content) {
        LOGGER.info("Update resource template {} for app {} associated to JVM {} in group {}", resourceTemplateName, appName, jvmName, groupName);
        LOGGER.debug(content);

        try {
            if (jvmName == null) {
                // TODO: Discuss with the team or users if updating a resource if a web app under a group means updating the resource of web apps under the JVMs as well.
                // Note: my 2 cents with the above comment is that it should be optional, e.g. the application should give the user
                //       means to indicate if the resource of a web app assigned to JVMs should be updated also.
                return ResponseBuilder.ok(groupService.updateGroupAppResourceTemplate(groupName, appName, resourceTemplateName, content));
            }
            return ResponseBuilder.ok(service.updateResourceTemplate(appName, resourceTemplateName, content, jvmName, groupName));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.debug("Failed to update resource template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response deployConf(final String appName, final String groupName, final String jvmName,
                               final String resourceTemplateName, final AuthenticatedUser authUser) {

        LOGGER.info("Deploying the application conf file {} for app {} to JVM {} in group {} by ", resourceTemplateName, appName, jvmName, groupName, authUser.getUser().getId());

        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName(resourceTemplateName)
                .setGroupName(groupName)
                .setWebAppName(appName)
                .setJvmName(jvmName)
                .build();
        resourceService.validateSingleResourceForGeneration(resourceIdentifier);
        
        final CommandOutput execData =
                service.deployConf(appName, groupName, jvmName, resourceTemplateName, resourceService.generateResourceGroup(), authUser.getUser());
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully deployed {} of {} to {} ", resourceTemplateName, appName, jvmName);
            return ResponseBuilder.ok("Successfully deployed " + resourceTemplateName + " of " + appName + " to "
                    + jvmName);
        } else {
            LOGGER.error("Failed to deploy application configuration [" + resourceTemplateName + "] for " + appName + " to " + jvmName + " :: " + execData.toString());
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc()));
        }
    }

    @Override
    public Response previewResourceTemplate(final String appName, final String groupName, final String jvmName,
                                            final String fileName,
                                            final String template) {
        LOGGER.debug("Preview resource template for app {} in group {} for JVM {} with content {}", appName, groupName, jvmName, template);
        try {
            return ResponseBuilder.ok(service.previewResourceTemplate(fileName, appName, groupName, jvmName, template, resourceService.generateResourceGroup()));
        } catch (RuntimeException rte) {
            LOGGER.debug("Error previewing template.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public static ApplicationServiceRest get() {
        return instance;
    }

    public void setMessageContext(MessageContext messageContext) {
        context = messageContext;
    }
}
