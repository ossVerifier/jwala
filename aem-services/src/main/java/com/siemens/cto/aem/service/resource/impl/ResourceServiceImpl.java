package com.siemens.cto.aem.service.resource.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.template.HarmonyTemplate;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.TemplateManager;

public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final TemplateManager templateManager;
    private final HarmonyTemplateEngine templateEngine;

    public ResourceServiceImpl(
            final TemplateManager theTemplateManager,
            final HarmonyTemplateEngine harmonyTemplateEngine
            ) {
        templateManager = theTemplateManager;
        templateEngine = harmonyTemplateEngine;
    }

    @Override
    public Collection<ResourceType> getResourceTypes() {
        try {
            Collection<ResourceType> resourceTypes = templateManager.getResourceTypes();
            for(ResourceType rtype : resourceTypes) {
                if(rtype.isValid()) {
                    HarmonyTemplate template = templateEngine.getTemplate(rtype);
                    try {
                        template.check();
                    } catch(Throwable exception) { 
                        rtype.setValid(false);
                        rtype.addException(exception);
                    }
                }
            }
            return resourceTypes;
        } catch (IOException e) {
            // This is extremely unlikely since we return ResourceTypes(valid=false) even when files are invalid. 
            String errorString = "Failed to get resource types from disk.";
            LOGGER.error(errorString, e);
            throw new FaultCodeException(AemFaultType.INVALID_PATH, errorString, e);
        }
    }
}
