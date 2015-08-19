package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;
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
import java.util.LinkedList;
import java.util.List;

public class ApplicationServiceRestImpl implements ApplicationServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebServerServiceRestImpl.class);

    private ApplicationService service;

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
        if(aGroupId != null) {
            apps = service.findApplications(aGroupId);
        } else {
            apps = service.getApplications();
        }
        return ResponseBuilder.ok(apps);
    }

    @Override
    public Response findApplicationsByJvmId(Identifier<Jvm> aJvmId) {
        LOGGER.debug("Find Apps requested with aJvmId: {}", aJvmId != null ? aJvmId : "null");
        if(aJvmId != null) {
            final List<Application> apps = service.findApplicationsByJvmId(aJvmId);
            return ResponseBuilder.ok(apps);
        } else {
            final List<Application> apps = service.getApplications();
            return ResponseBuilder.ok(apps);
        }
    }

    @Override
    public Response createApplication(final JsonCreateApplication anAppToCreate,
                                      final AuthenticatedUser aUser) {
        LOGGER.debug("Create Application requested: {}", anAppToCreate);
        Application created = service.createApplication(anAppToCreate.toCreateCommand(),
                                                        aUser.getUser());
        return ResponseBuilder.created(created);
    }

    @Override
    public Response updateApplication(final JsonUpdateApplication anAppToUpdate,
                                      final AuthenticatedUser aUser) {
        LOGGER.debug("Update Application requested: {}", anAppToUpdate);
        Application updated = service.updateApplication(anAppToUpdate.toUpdateCommand(),
                                                        aUser.getUser());
        return ResponseBuilder.ok(updated);
    }

    @Override
    public Response removeApplication(final Identifier<Application> anAppToRemove,
                                      final AuthenticatedUser aUser) {
        LOGGER.debug("Delete JVM requested: {}", anAppToRemove);
        service.removeApplication(anAppToRemove,
                                  aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Context
    private MessageContext context;

    @Override
    public Response uploadWebArchive(final Identifier<Application> anAppToGet,
                                     final AuthenticatedUser aUser) {
        LOGGER.debug("Upload Archive requested: {} streaming (no size, count yet)", anAppToGet);

        // iframe uploads from IE do not understand application/json as a response and will prompt for download. Fix: return text/html
        if(!context.getHttpHeaders().getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            context.getHttpServletResponse().setContentType(MediaType.TEXT_HTML);
        }

        Application app = service.getApplication(anAppToGet);

        ServletFileUpload sfu = new ServletFileUpload();
        InputStream data = null;
        try {
            FileItemIterator iter = sfu.getItemIterator( context.getHttpServletRequest() );
            FileItemStream file1;

            while(iter.hasNext()) {
                file1 =  iter.next();
                try {
                    data = file1.openStream();

                    UploadWebArchiveCommand command = new UploadWebArchiveCommand(app,
                        file1.getName(),
                        -1L,
                        data);

                    return ResponseBuilder.created(service.uploadWebArchive(command,
                                                                            aUser.getUser())); // early out on first attachment
                } finally {
                    data.close();
                }
            }
            return ResponseBuilder.notOk(Status.NO_CONTENT, new FaultCodeException(AemFaultType.INVALID_APPLICATION_WAR, "No data"));
        } catch (IOException | FileUploadException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Error receiving data", e);
        }
    }

    @Override
    public Response deleteWebArchive(final Identifier<Application> appToRemoveWAR,
                                     final AuthenticatedUser aUser) {
        LOGGER.debug("Delete Archive requested: {}", appToRemoveWAR);

        Application updated = service.deleteWebArchive(appToRemoveWAR,
                                                       aUser.getUser());

        return ResponseBuilder.ok(updated);
    }

    @Override
    public Response getResourceNames(String appName) {
        // TODO: Get resource names from db.
        final List<String> resources = new LinkedList<>();
        resources.add(appName + ".xml");
        return ResponseBuilder.ok(resources);
    }
}
