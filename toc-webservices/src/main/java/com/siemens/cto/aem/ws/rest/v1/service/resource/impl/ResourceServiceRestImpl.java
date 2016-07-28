package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.resource.ContentType;
import com.siemens.cto.aem.common.domain.model.resource.ResourceContent;
import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.CreateResourceResponseWrapper;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.resource.CreateResourceParam;
import com.siemens.cto.aem.ws.rest.v1.service.resource.ResourceHierarchyParam;
import com.siemens.cto.aem.ws.rest.v1.service.resource.ResourceServiceRest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * {@link ResourceServiceRest} implementation.
 * <p/>
 * Created by z003e5zv on 3/16/2015.
 */
public class ResourceServiceRestImpl implements ResourceServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServiceRestImpl.class);
    private static final int CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS = 2;
    private static final String JSON_FILE_EXTENSION = ".json";
    public static final String UNEXPECTED_CONTENT_TYPE_ERROR_MSG =
            "File being uploaded is invalid! The expected file type as indicated in the meta data is text based and should have a TPL extension.";
    public static final String TPL_FILE_EXTENSION = ".tpl";

    private final ResourceService resourceService;

    public ResourceServiceRestImpl(final ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    public Response createTemplate(final List<Attachment> attachments, final String targetName, final AuthenticatedUser user) {
        LOGGER.info("create template for target {} by user {}", targetName, user.getUser().getId());
        try {
            // TODO check for a max file size
            List<Attachment> filteredAttachments = new ArrayList<>();
            for (Attachment attachment : attachments) {
                if (attachment.getDataHandler().getName() != null) {
                    filteredAttachments.add(attachment);
                }
            }
            if (filteredAttachments.size() == CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS) {
                InputStream metadataInputStream = null;
                InputStream templateInputStream = null;
                for (Attachment attachment : filteredAttachments) {
                    final DataHandler handler = attachment.getDataHandler();
                    try {
                        LOGGER.debug("filename is {}", handler.getName());
                        if (handler.getName().toLowerCase().endsWith(JSON_FILE_EXTENSION)) {
                            metadataInputStream = attachment.getDataHandler().getInputStream();
                        } else {
                            templateInputStream = attachment.getDataHandler().getInputStream();
                        }
                    } catch (final IOException ioe) {
                        LOGGER.error("Create template failed!", ioe);
                        return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                new FaultCodeException(AemFaultType.IO_EXCEPTION, ioe.getMessage()));
                    }
                }
                return ResponseBuilder.created(resourceService.createTemplate(metadataInputStream, templateInputStream, targetName, user.getUser()));
            } else {
                return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                        AemFaultType.INVALID_NUMBER_OF_ATTACHMENTS,
                        "Invalid number of attachments! 2 attachments is expected by the service."));
            }
        } catch (final ResourceServiceException rse) {
            LOGGER.error("Remove template failed!", rse);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.SERVICE_EXCEPTION, rse.getMessage()));
        }
    }

    @Override
    public Response getResourceAttrData() {
        LOGGER.debug("Get resource attribute data");
        return ResponseBuilder.ok(resourceService.generateResourceGroup());
    }

    @Override
    public Response getResourceTopology() {
        LOGGER.debug("Get resource topology");
        return ResponseBuilder.ok(resourceService.generateResourceGroup());
    }

    @Override
    public Response getApplicationResourceNames(final String groupName, final String appName) {
        LOGGER.debug("Get application resource name for group {} and application {}", groupName, appName);
        return ResponseBuilder.ok(resourceService.getApplicationResourceNames(groupName, appName));
    }

    @Override
    public Response getAppTemplate(final String groupName, final String appName, final String templateName) {
        LOGGER.debug("Get application template for group {}, application {}, and template {}", groupName, appName, templateName);
        return ResponseBuilder.ok(resourceService.getAppTemplate(groupName, appName, templateName));
    }

    @Override
    public Response checkFileExists(final String groupName, final String jvmName, final String webappName, final String webserverName, final String fileName) {
        LOGGER.debug("Check file exists for group {}, JVM {}, application {}, web server {}, file {}", groupName, jvmName, webappName, webserverName, fileName);
        return ResponseBuilder.ok(resourceService.checkFileExists(groupName, jvmName, webappName, webserverName, fileName));
    }

    @Override
    // TODO: Re validation, maybe we can use CXF bean validation ?
    public Response createResource(final List<Attachment> attachments, final CreateResourceParam createResourceParam,
                                   final AuthenticatedUser user) {

        LOGGER.info("Create resource with parameters {} by user {} and attachments {}", createResourceParam, user.getUser().getId(), attachments);

        InputStream metadataIn = null;
        InputStream resourceDataIn = null;

        String fileName = StringUtils.EMPTY;

        final List<Attachment> filteredAttachments = new ArrayList<>();
        for (Attachment attachment : attachments) {
            if (attachment.getDataHandler().getName() != null) {
                filteredAttachments.add(attachment);
            }
        }

        if (filteredAttachments.size() == CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS) {
            for (Attachment attachment : filteredAttachments) {
                final DataHandler handler = attachment.getDataHandler();
                try {
                    LOGGER.debug("filename is {}", handler.getName());
                    if (handler.getName().toLowerCase().endsWith(JSON_FILE_EXTENSION)) {
                        metadataIn = attachment.getDataHandler().getInputStream();
                    } else {
                        fileName = attachment.getDataHandler().getName();
                        resourceDataIn = attachment.getDataHandler().getInputStream();
                    }
                } catch (final IOException ioe) {
                    LOGGER.error("Create template failed!", ioe);
                    return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                            new FaultCodeException(AemFaultType.IO_EXCEPTION, ioe.getMessage()));
                }
            }
        } else {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_NUMBER_OF_ATTACHMENTS,
                    "Invalid number of attachments! 2 attachments is expected by the service."));
        }

        CreateResourceResponseWrapper responseWrapper = null;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final ResourceTemplateMetaData metaData =  mapper.readValue(IOUtils.toString(metadataIn),
                                                                        ResourceTemplateMetaData.class);

            // We do the file attachment validation here since this is a REST services affair IMHO.
            // TODO: Use a more sophisticated way of knowing the content type in next releases.
            if (!ContentType.APPLICATION_BINARY.contentTypeStr.equalsIgnoreCase(metaData.getContentType()) &&
                    !(fileName.toLowerCase().endsWith(TPL_FILE_EXTENSION))) {
                LOGGER.error(UNEXPECTED_CONTENT_TYPE_ERROR_MSG);
                return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                        new FaultCodeException(AemFaultType.SERVICE_EXCEPTION, UNEXPECTED_CONTENT_TYPE_ERROR_MSG));
            }

            final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder().setResourceName(metaData.getTemplateName())
                                                                                          .setGroupName(createResourceParam.getGroup())
                                                                                          .setWebServerName(createResourceParam.getWebServer())
                                                                                          .setJvmName(createResourceParam.getJvm())
                                                                                          .setWebAppName(createResourceParam.getWebApp()).build();

            // Upload the attached file if the resource is binary to the archive location
            if (metaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)){
                resourceDataIn = new ByteArrayInputStream(resourceService.uploadResource(metaData, resourceDataIn).getBytes());
            }

            responseWrapper = resourceService.createResource(resourceIdentifier, metaData, resourceDataIn);
            if (responseWrapper == null) {
                // TODO: Review response...
                return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                        new FaultCodeException(AemFaultType.INVALID_REST_SERVICE_PARAMETER,
                                "There was no resource handler to process the request!"));
            }
        } catch (final IOException ioe) {
            LOGGER.warn("exception thrown in CreateResource: {}", ioe);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.SERVICE_EXCEPTION, ioe.getMessage()));
        } catch (final ResourceServiceException rse) {
            LOGGER.error("exception thrown in CreateResource: {}", rse);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.SERVICE_EXCEPTION, rse.getMessage()));
        }
        return ResponseBuilder.ok(responseWrapper);
    }

    @Override
    // TODO: Re validation, maybe we can use CXF bean validation ?
    public Response deleteResource(final String templateName, final ResourceHierarchyParam resourceHierarchyParam, final AuthenticatedUser aUser) {
        LOGGER.info("Delete resource {} by user {} with details {}", templateName, aUser.getUser().getId(), resourceHierarchyParam);
        int deletedRecCount = 0;

        // NOTE: We do the parameter checking logic here since the service layer does not know anything about ResourceHierarchyParam.
        if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level Web App
            deletedRecCount = resourceService.deleteGroupLevelAppResource(resourceHierarchyParam.getWebApp(), templateName);

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web App
            deletedRecCount = resourceService.deleteAppResource(templateName, resourceHierarchyParam.getWebApp(), resourceHierarchyParam.getJvm());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {
            // Group Level Web Servers
            if (resourceHierarchyParam.getWebServer().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelWebServerResource(templateName, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web Server
            deletedRecCount = resourceService.deleteWebServerResource(templateName, resourceHierarchyParam.getWebServer());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level JVMs
            if (resourceHierarchyParam.getJvm().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelJvmResource(templateName, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // JVM
            deletedRecCount = resourceService.deleteJvmResource(templateName, resourceHierarchyParam.getJvm());

        } else {

            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.INVALID_REST_SERVICE_PARAMETER,
                            "Parameters passed to the rest service is/are invalid!"));

        }
        return ResponseBuilder.ok(deletedRecCount);
    }

    @Override
    public Response deleteResources(final String[] templateNameArray, ResourceHierarchyParam resourceHierarchyParam, AuthenticatedUser user) {
        LOGGER.info("Delete resources {} by user {} with details {}", templateNameArray, user.getUser().getId(), resourceHierarchyParam);
        int deletedRecCount = 0;

        final List<String> templateNameList = Arrays.asList(templateNameArray);

        // NOTE: We do the parameter checking logic here since the service layer does not know anything about ResourceHierarchyParam.
        if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level Web App
            deletedRecCount = resourceService.deleteGroupLevelAppResources(resourceHierarchyParam.getWebApp(), resourceHierarchyParam.getGroup(), templateNameList);

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web App
            deletedRecCount = resourceService.deleteAppResources(templateNameList, resourceHierarchyParam.getWebApp(), resourceHierarchyParam.getJvm());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {
            // Group Level Web Servers
            if (resourceHierarchyParam.getWebServer().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelWebServerResources(templateNameList, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web Server
            deletedRecCount = resourceService.deleteWebServerResources(templateNameList, resourceHierarchyParam.getWebServer());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level JVMs
            if (resourceHierarchyParam.getJvm().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelJvmResources(templateNameList, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // JVM
            deletedRecCount = resourceService.deleteJvmResources(templateNameList, resourceHierarchyParam.getJvm());

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()){
            // External Properties
            deletedRecCount = resourceService.deleteExternalProperties();

        } else {

            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.INVALID_REST_SERVICE_PARAMETER,
                            "Parameters passed to the rest service is/are invalid!"));

        }
        return ResponseBuilder.ok(deletedRecCount);
    }

    @Override
    public Response getResourceContent(final String resourceName, final ResourceHierarchyParam param) {
        LOGGER.debug("Get the resource content for {} with hierarchy {}", resourceName, param);
        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName)
                                                                                      .setGroupName(param.getGroup())
                                                                                      .setWebServerName(param.getWebServer())
                                                                                      .setJvmName(param.getJvm())
                                                                                      .setWebAppName(param.getWebApp()).build();
        final ResourceContent resourceContent = resourceService.getResourceContent(resourceIdentifier);
        if (resourceContent == null) {
            return Response.noContent().build();
        }
        return ResponseBuilder.ok(resourceContent);
    }

    @Override
    public Response updateResourceContent(String resourceName, ResourceHierarchyParam resourceHierarchyParam, String templateContent) {
        LOGGER.info("Update the resource {} with hierarchy {}", resourceName, resourceHierarchyParam);
        LOGGER.debug("Updated content: {}", templateContent);

        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName)
                .setGroupName(resourceHierarchyParam.getGroup())
                .setWebServerName(resourceHierarchyParam.getWebServer())
                .setJvmName(resourceHierarchyParam.getJvm())
                .setWebAppName(resourceHierarchyParam.getWebApp()).build();

        return ResponseBuilder.ok(resourceService.updateResourceContent(resourceIdentifier, templateContent));
    }

    @Override
    public Response previewResourceContent(final ResourceHierarchyParam resourceHierarchyParam, String content) {
        LOGGER.debug("Preview the template for {}", resourceHierarchyParam);
        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setGroupName(resourceHierarchyParam.getGroup())
                .setWebServerName(resourceHierarchyParam.getWebServer())
                .setJvmName(resourceHierarchyParam.getJvm())
                .setWebAppName(resourceHierarchyParam.getWebApp()).build();
        return ResponseBuilder.ok(resourceService.previewResourceContent(resourceIdentifier, content));
    }

    @Override
    public Response uploadExternalProperties(final Attachment attachment, final AuthenticatedUser user) {
        LOGGER.info("Upload external resource by user {} and attachments {}", user.getUser().getId(), attachment);

        InputStream propertiesFileIn = null;
        String fileName = StringUtils.EMPTY;

        final DataHandler handler = attachment.getDataHandler();
        try {
            LOGGER.debug("filename is {}", handler.getName());
            fileName = handler.getName();
            propertiesFileIn = handler.getInputStream();
        } catch (final IOException ioe) {
            LOGGER.error("Create external properties failed!", ioe);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(AemFaultType.IO_EXCEPTION, ioe.getMessage()));
        }

        return ResponseBuilder.ok(resourceService.uploadExternalProperties(fileName, propertiesFileIn));
    }

    @Override
    // TODO remove this and replace with generic resource.get()
    public Response getExternalPropertiesFile() {
        LOGGER.debug("Get the external properties file name");
        final String externalPropertiesFile = resourceService.getExternalPropertiesFile();
        List<String> propertiesFile = new ArrayList<>();
        if (!externalPropertiesFile.isEmpty()) {
            propertiesFile.add(externalPropertiesFile);
        }
        return ResponseBuilder.ok(propertiesFile);
    }

    @Override
    public Response getExternalProperties() {
        LOGGER.debug("Get the external properties");
        // use a TreeMap to put the properties in alphabetical order
        final Properties externalProperties = resourceService.getExternalProperties();
        return ResponseBuilder.ok(null == externalProperties ? null : new TreeMap<>(externalProperties));
    }
}
