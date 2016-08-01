package com.siemens.cto.aem.ws.rest.v1.service.admin.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.admin.AdminServiceRest;
import com.siemens.cto.toc.files.FilesConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class AdminServiceRestImpl implements AdminServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(AdminServiceRestImpl.class);
    public static final String JSON_RESPONSE_TRUE = "true";
    public static final String JSON_RESPONSE_FALSE = "false";

    private static final String TOC_AUTHORIZATION= "toc.authorization";


    private FilesConfiguration filesConfiguration;
    private ResourceService resourceService;

    @Autowired
    PropertySourcesPlaceholderConfigurer configurer;

    public AdminServiceRestImpl(FilesConfiguration theFilesConfiguration, ResourceService resourceService) {
        this.filesConfiguration = theFilesConfiguration;
        this.resourceService = resourceService;
    }

    @Override
    public Response reload() {
        ApplicationProperties.reload();
        Properties copyToReturn = ApplicationProperties.getProperties();
        configurer.setProperties(copyToReturn);

        filesConfiguration.reload();

        String logProperties = System.getProperty("log4j.configuration", "");
        if (logProperties.endsWith(".xml")) {
            URL logPropsUrl = getClass().getClassLoader().getResource(logProperties);
            if (logPropsUrl != null) {
                LOGGER.info("Reloading logging configuration from " + logProperties);
                copyToReturn.put("logging-reload-state", "failed reload from " + logPropsUrl.toString());
                LogManager.resetConfiguration();
                DOMConfigurator.configure(logPropsUrl);
                copyToReturn.put("logging-reload-state", "reloaded from " + logPropsUrl.toString());
            } else {
                LOGGER.warn("Could not reload logging configuration from " + logProperties);
                copyToReturn.put("logging-reload-state", "failed, could not locate: " + logProperties);
            }
        } else if (logProperties.endsWith(".properties")) {
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(logProperties);
            copyToReturn.put("logging-reload-state", "reloaded from " + logProperties);
        } else {
            LOGGER.info("Reloading logging configuration from ApplicationProperties...");
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(ApplicationProperties.getProperties());
            copyToReturn.put("logging-reload-state", "reloaded from properties");
        }
        return ResponseBuilder.ok(new TreeMap<>(copyToReturn));
    }

    @Override
    public Response view() {
        return ResponseBuilder.ok(new TreeMap<>(ApplicationProperties.getProperties()));
    }


    @Override
    public Response encrypt(String cleartext) {

        if (cleartext == null || cleartext.trim().length() == 0) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return ResponseBuilder.ok(resourceService.encryptUsingPlatformBean(cleartext));
        }
    }

    @Override
    public Response manifest(ServletContext context) {
        Attributes attributes = null;
        if (context != null) {
            try {
                InputStream manifestStream = context.getResourceAsStream("META-INF/MANIFEST.MF");
                Manifest manifest = new Manifest(manifestStream);
                attributes = manifest.getMainAttributes();
            } catch (IOException e) {
                LOGGER.debug("Error getting manifest for " + context.getServletContextName(), e);
                throw new InternalErrorException(AemFaultType.INVALID_PATH, "Failed to read MANIFEST.MF for "
                        + context.getServletContextName());
            }
        }
        return ResponseBuilder.ok(attributes);
    }

    @Override
    public Response isTOCAuthorizationEnabled() {
        String auth = ApplicationProperties.get(TOC_AUTHORIZATION, "true");
        if("false".equals(auth))
            return ResponseBuilder.ok(JSON_RESPONSE_FALSE);
        else 
            return ResponseBuilder.ok(JSON_RESPONSE_TRUE);
    }
    
}
