package com.siemens.cto.aem.ws.rest.v1.service.admin.impl;

import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.admin.AdminServiceRest;
import com.siemens.cto.toc.files.FilesConfiguration;

public class AdminServiceRestImpl implements AdminServiceRest {

    private FilesConfiguration filesConfiguration;
    
    public AdminServiceRestImpl(FilesConfiguration theFilesConfiguration) {
        this.filesConfiguration = theFilesConfiguration;
    }
    
    @Override
    public Response reload() {
        ApplicationProperties.reload();
        filesConfiguration.reload();
        
        String logProperties = System.getProperty("log4j.configuration","");
        if(logProperties.endsWith(".xml")) {
            LogManager.resetConfiguration();
            DOMConfigurator.configure(logProperties);
        } else if(logProperties.endsWith(".properties")){ 
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(logProperties);
        } else { 
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(ApplicationProperties.getProperties());
        }
        return ResponseBuilder.ok(ApplicationProperties.getProperties());
    }

    @Override
    public Response view() {
        return ResponseBuilder.ok(ApplicationProperties.getProperties());
    }
}
