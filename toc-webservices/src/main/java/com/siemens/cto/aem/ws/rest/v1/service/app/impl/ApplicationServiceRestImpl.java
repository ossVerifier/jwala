package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.siemens.cto.aem.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.exec.CommandOutput;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;

public class ApplicationServiceRestImpl implements ApplicationServiceRest {

    private final static Logger logger = LoggerFactory.getLogger(WebServerServiceRestImpl.class);

    private ApplicationService service;

    public ApplicationServiceRestImpl(ApplicationService applicationService) {
        service = applicationService;
    }

    @Override
    public Response getApplication(Identifier<Application> anAppId) {
        logger.debug("Get App by id: {}", anAppId);
        final Application app = service.getApplication(anAppId);
        return ResponseBuilder.ok(app);
    }

    @Override
    public Response getApplications(Identifier<Group> aGroupId) {
        logger.debug("Get Apps requested with groupId: {}", aGroupId != null ? aGroupId : "null");
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
        logger.debug("Find Apps requested with aJvmId: {}", aJvmId != null ? aJvmId : "null");
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
        logger.debug("Create Application requested: {}", anAppToCreate);
        Application created = service.createApplication(anAppToCreate.toCreateCommand(), aUser.getUser());
        return ResponseBuilder.created(created);
    }

    @Override
    public Response updateApplication(final JsonUpdateApplication anAppToUpdate, final AuthenticatedUser aUser) {
        logger.debug("Update Application requested: {}", anAppToUpdate);
        Application updated = service.updateApplication(anAppToUpdate.toUpdateCommand(), aUser.getUser());
        return ResponseBuilder.ok(updated);
    }

    @Override
    public Response removeApplication(final Identifier<Application> anAppToRemove, final AuthenticatedUser aUser) {
        logger.debug("Delete JVM requested: {}", anAppToRemove);
        service.removeApplication(anAppToRemove, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Context
    private MessageContext context;

    @Override
    public Response uploadWebArchive(final Identifier<Application> anAppToGet, final AuthenticatedUser aUser) {
        logger.debug("Upload Archive requested: {} streaming (no size, count yet)", anAppToGet);

        // iframe uploads from IE do not understand application/json
        // as a response and will prompt for download. Fix: return
        // text/html
        if (!context.getHttpHeaders().getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            context.getHttpServletResponse().setContentType(MediaType.TEXT_HTML);
        }

        Application app = service.getApplication(anAppToGet);

        ServletFileUpload sfu = new ServletFileUpload();
        InputStream data = null;
        try {
            FileItemIterator iter = sfu.getItemIterator(context.getHttpServletRequest());
            FileItemStream file1;

            while (iter.hasNext()) {
                file1 = iter.next();
                try {
                    data = file1.openStream();

                    UploadWebArchiveRequest command = new UploadWebArchiveRequest(app, file1.getName(), -1L, data);

                    final Application application = service.uploadWebArchive(command, aUser.getUser());
                    service.copyApplicationWarToGroupHosts(application, new RuntimeCommandBuilder());

                    return ResponseBuilder.created(application); // early
                                                                 // out
                                                                 // on
                                                                 // first
                                                                 // attachment
                } finally {
                    data.close();
                }
            }
            return ResponseBuilder.notOk(Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_APPLICATION_WAR, "No data"));
        } catch (IOException | FileUploadException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    @Override
    public Response deleteWebArchive(final Identifier<Application> appToRemoveWAR, final AuthenticatedUser aUser) {
        logger.debug("Delete Archive requested: {}", appToRemoveWAR);

        Application updated = service.deleteWebArchive(appToRemoveWAR, aUser.getUser());

        return ResponseBuilder.ok(updated);
    }

    @Override
    public Response getResourceNames(final String appName) {
        return ResponseBuilder.ok(service.getResourceTemplateNames(appName));
    }

    @Override
    public Response getResourceTemplate(final String appName, final String groupName, final String jvmName,
            final String resourceTemplateName, final boolean tokensReplaced) {
        return ResponseBuilder.ok(service.getResourceTemplate(appName, groupName, jvmName, resourceTemplateName,
                tokensReplaced));
    }

    @Override
    public Response updateResourceTemplate(final String appName, final String resourceTemplateName,
            final String content) {

        try {
            return ResponseBuilder.ok(service.updateResourceTemplate(appName, resourceTemplateName, content));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            logger.debug("Failed to update resource template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.PERSISTENCE_ERROR, e.getMessage()));
        }

    }

    @Override
    public Response deployConf(final String appName, final String groupName, final String jvmName,
            final String resourceTemplateName, final AuthenticatedUser authUser) {
        try {
            final CommandOutput execData =
                    service.deployConf(appName, groupName, jvmName, resourceTemplateName, authUser.getUser());
            if (execData.getReturnCode().wasSuccessful()) {
                return ResponseBuilder.ok("Successfully deployed " + resourceTemplateName + " of " + appName + " to "
                        + jvmName);
            } else {
                return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.REMOTE_COMMAND_FAILURE, execData.toString()));
            }
        } catch (RuntimeException re) {
            logger.error("Exception deploying application configuration", re);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.REMOTE_COMMAND_FAILURE, re.getMessage()));
        }

    }

    @Override
    public Response uploadConfigTemplate(String appName, AuthenticatedUser aUser, String appXmlFileName) {
        logger.debug("Upload Archive requested: {} streaming (no size, count yet)", appName);

        // iframe uploads from IE do not understand application/json
        // as a response and will prompt for download. Fix: return
        // text/html
        if (!context.getHttpHeaders().getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            context.getHttpServletResponse().setContentType(MediaType.TEXT_HTML);
        }

        List<Application> applications = service.getApplications();
        Application app = null;
        for (Application resultApp : applications) {
            if (resultApp.getName().equals(appName)) {
                app = resultApp;
                break;
            }
        }
        if (null == app) {
            throw new InternalErrorException(AemFaultType.APPLICATION_NOT_FOUND,
                    "Could not find Application with name " + appName);
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
                    UploadAppTemplateRequest command =
                            new UploadAppTemplateRequest(app, file1.getName(), appXmlFileName, data);

                    return ResponseBuilder.created(service.uploadAppTemplate(command, aUser.getUser())); // early
                                                                                                         // out
                                                                                                         // on
                                                                                                         // first
                                                                                                         // attachment
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

    @Override
    public Response previewResourceTemplate(final String appName, final String groupName, final String jvmName,
            final String template) {
        try {
            return ResponseBuilder.ok(service.previewResourceTemplate(appName, groupName, jvmName, template));
        } catch (RuntimeException rte) {
            logger.debug("Error previewing template.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }
}
