package com.siemens.cto.aem.service.resource;

import java.util.Collection;

import com.siemens.cto.aem.domain.model.resource.ResourceType;


public interface ResourceService {

    Collection<ResourceType> getResourceTypes();
}
