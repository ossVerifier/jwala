package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.CommandOutputReturnCode;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class ApplicationServiceRestImpl implements ApplicationServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceRestImpl.class);

    private ApplicationService service;
    private static ApplicationServiceRestImpl instance;

    public ApplicationServiceRestImpl(ApplicationService applicationService) {
        service = applicationService;
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
        Application created = service.createApplication(anAppToCreate.toCreateCommand(), aUser.getUser());
        return ResponseBuilder.created(created);
    }

    @Override
    public Response updateApplication(final JsonUpdateApplication anAppToUpdate, final AuthenticatedUser aUser) {
        LOGGER.info("Update Application requested: {}", anAppToUpdate);
        Application updated = service.updateApplication(anAppToUpdate.toUpdateCommand(), aUser.getUser());
        return ResponseBuilder.ok(updated);
    }

    @Override
    public Response removeApplication(final Identifier<Application> anAppToRemove, final AuthenticatedUser aUser) {
        LOGGER.debug("Delete JVM requested: {}", anAppToRemove);
        service.removeApplication(anAppToRemove, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Context
    private MessageContext context;

    @Override
    public Response uploadWebArchive(final Identifier<Application> anAppToGet, final AuthenticatedUser aUser) {
        LOGGER.info("Upload Archive requested: {} streaming (no size, count yet)", anAppToGet);

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

                    LOGGER.info("Upload file {} to application {}", file1.getName(), app.getName());
                    final Application application = service.uploadWebArchive(command, aUser.getUser());
                    LOGGER.info("Upload succeeded");

                    return ResponseBuilder.created(application); // early out on first attachment
                } catch (InternalErrorException ie){
                    LOGGER.error("Caught an internal error exception that would normally get out and converting to a response {}", ie);
                    return ResponseBuilder.notOk(Status.INTERNAL_SERVER_ERROR, ie);
                } finally {
                    data.close();
                }
            }
            LOGGER.info("Returning No Data response for application war upload {}", app.getName());
            return ResponseBuilder.notOk(Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_APPLICATION_WAR, "No data"));
        } catch (IOException | FileUploadException e) {
            LOGGER.error("Bad Stream: Error receiving data", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    @Override
    public Response deleteWebArchive(final Identifier<Application> appToRemoveWAR, final AuthenticatedUser aUser) {
        LOGGER.debug("Delete Archive requested: {}", appToRemoveWAR);

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
        if (null != jvms && jvms.size() > 0) {
            for (Jvm jvm : jvms){
                if (jvm.getState().isStartedState()){
                    final String jvmName = jvm.getJvmName();
                    LOGGER.error("The JVM {} must be stopped before deploying the application", jvmName);
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "The JVM " + jvmName + " must be stopped before attempting to deploy application " + appName);
                }
            }
            service.copyApplicationWarToGroupHosts(app);
            service.copyApplicationConfigToGroupJvms(group, appName, aUser.getUser());
        } else {
            LOGGER.info("Skip deploying application {}, no JVM's in group {}", appName, group.getName() );
        }
        return ResponseBuilder.ok(app);
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
    public Response updateResourceTemplate(final String appName,
                                           final String resourceTemplateName,
                                           final String jvmName,
                                           final String groupName,
                                           final String content) {

        try {
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

        final boolean doBackUpBeforeCopy = true;
        final CommandOutput execData =
                service.deployConf(appName, groupName, jvmName, resourceTemplateName, doBackUpBeforeCopy, authUser.getUser());
        if (execData.getReturnCode().wasSuccessful()) {
            if (resourceTemplateName.endsWith(".properties")) {
                // deploy the application.properties and roleMapping.properties to the other hosts in the group
                service.deployConfToOtherJvmHosts(appName, groupName, jvmName, resourceTemplateName, authUser.getUser());
            }
            return ResponseBuilder.ok("Successfully deployed " + resourceTemplateName + " of " + appName + " to "
                    + jvmName);
        } else {
            LOGGER.error("Failed to deploy application configuration [" + resourceTemplateName + "] for " + appName + " to " + jvmName + " :: " + execData.toString());
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.REMOTE_COMMAND_FAILURE, CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc()));
        }
    }

    @Override
    public Response uploadConfigTemplate(String appName, AuthenticatedUser aUser, String appXmlFileName, String jvmName) {
        LOGGER.debug("Upload Archive requested: {} streaming (no size, count yet)", appName);

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
            LOGGER.error("Application Not Found: Could not find Application with name " + appName);
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
                            new UploadAppTemplateRequest(app, file1.getName(), appXmlFileName, jvmName, data);

                    service.uploadAppTemplate(command);
                    // return the template after uploading because returning the JpaAppConfigTemplate was returning an unreadable json object - so unreadable that it would actually crash editors when copied from the chrome debugger and pasted
                    return ResponseBuilder.created(service.getResourceTemplate(appName, app.getGroup().getName(), jvmName, appXmlFileName, false)); // early out on first attachment
                } finally{
                    assert data != null;
                    data.close();
                }
            }
            LOGGER.error("No content in uploaded file {} to JVM: {}", appXmlFileName, jvmName);
            return ResponseBuilder.notOk(Response.Status.NO_CONTENT, new FaultCodeException(
                    AemFaultType.INVALID_JVM_OPERATION, "No data"));
        } catch (IOException | FileUploadException e) {
            LOGGER.error("Bad Stream: Error receiving data", e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    @Override
    public Response previewResourceTemplate(final String appName, final String groupName, final String jvmName,
                                            final String template) {
        try {
            return ResponseBuilder.ok(service.previewResourceTemplate(appName, groupName, jvmName, template));
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
