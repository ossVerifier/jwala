package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItem;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;

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
    public Response getApplications(Identifier<Group> aGroupId, PaginationParamProvider paginationParamProvider) {
        LOGGER.debug("Get Apps requested with pagination: {}, groupId: {}", paginationParamProvider, aGroupId != null ? aGroupId : "null");
        final List<Application> apps; 
        PaginationParameter page = paginationParamProvider.getPaginationParameter();
        if(aGroupId != null) {
            apps = service.findApplications(aGroupId, page);
        } else {
            apps = service.getApplications(page);
        }
        return ResponseBuilder.ok(apps); 
    }

    @Override
    public Response findApplicationsByJvmId(Identifier<Jvm> aJvmId, PaginationParamProvider paginationParamProvider) {
        LOGGER.debug("Find Apps requested with pagination: {}, aJvmId: {}", paginationParamProvider, aJvmId != null ? aJvmId : "null");
        PaginationParameter page = paginationParamProvider.getPaginationParameter();
        if(aJvmId != null) {
            final List<Application> apps = service.findApplicationsByJvmId(aJvmId, page);
            return ResponseBuilder.ok(apps);
        } else {
            final List<Application> apps = service.getApplications(page);
            return ResponseBuilder.ok(apps);
        }
    }

    @Override
    public Response createApplication(JsonCreateApplication anAppToCreate) {
        LOGGER.debug("Create Application requested: {}", anAppToCreate);            
        Application created = service.createApplication(anAppToCreate.toCreateCommand(),
                User.getHardCodedUser());
        return ResponseBuilder.created(created);
    }

    @Override
    public Response updateApplication(final JsonUpdateApplication anAppToUpdate) {
        LOGGER.debug("Update Application requested: {}", anAppToUpdate);
        Application updated = service.updateApplication(anAppToUpdate.toUpdateCommand(),
                User.getHardCodedUser());
        return ResponseBuilder.ok(updated);
    }

    @Override
    public Response removeApplication(final Identifier<Application> anAppToRemove) {
        LOGGER.debug("Delete JVM requested: {}", anAppToRemove);
        service.removeApplication(anAppToRemove, User.getHardCodedUser());
        return ResponseBuilder.ok();
    }

    @Context 
    private MessageContext context;

    @Override
    public Response uploadWebArchive(Identifier<Application> anAppToGet, List<Attachment> attachments) {
        LOGGER.debug("Upload Archive requested: {} attachmentCount {}", anAppToGet, attachments.size());
        
        if(attachments.size() != 1) { 
            return ResponseBuilder.notOk(Status.BAD_REQUEST, new FaultCodeException(AemFaultType.BAD_STREAM, "Just one attachment please."));
        }
        
        Application app = service.getApplication(anAppToGet);
        
        Attachment archive = attachments.get(0);
        
        ServletFileUpload sfu = new ServletFileUpload();
        List<FileItem> fileItems = sfu.parseRequest( context.getHttpServletRequest() );
        FileItem file1 = fileItems.get(0);
        try {
            new UploadWebArchiveCommand(app,
                archive.getContentDisposition().getParameter("filename"),
                (int) file1.getSize(),
                archive.getDataHandler().getInputStream());
        } /* catch (NumberFormatException e) {
            throw new BadRequestException(AemFaultType.BAD_STREAM, "Invalid or no length specified", e);
            // TODO return ResponseBuilder.notOk(Status.LENGTH_REQUIRED);
        } */ catch (IOException e) {
            throw new InternalErrorException(AemFaultType.BAD_STREAM, "Erorr receiving data", e);
        }
        
        return ResponseBuilder.created(app);
    }
}
