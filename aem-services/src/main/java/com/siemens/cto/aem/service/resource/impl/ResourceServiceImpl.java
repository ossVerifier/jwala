package com.siemens.cto.aem.service.resource.impl;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.toc.files.TemplateManager;

public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final TemplateManager templateManager;

    public ResourceServiceImpl(final TemplateManager theTemplateManager) {
        templateManager = theTemplateManager;
    }

    @Override
    public Collection<ResourceType> getResourceTypes() {
        try {
            return templateManager.getResourceTypes();
        } catch (IOException e) {
            String errorString = "Failed to get resource types from disk.";
            LOGGER.error(errorString, e);
            throw new FaultCodeException(AemFaultType.INVALID_PATH, errorString, e);
        }
    }
}
