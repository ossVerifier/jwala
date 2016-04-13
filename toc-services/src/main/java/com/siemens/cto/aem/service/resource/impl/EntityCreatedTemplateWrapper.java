package com.siemens.cto.aem.service.resource.impl;

import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.service.resource.CreatedTemplateWrapper;

/**
 * Wrapper for a created template for an entity.
 *
 * Created by JC043760 on 4/13/2016.
 */
public class EntityCreatedTemplateWrapper implements CreatedTemplateWrapper {

    final private ConfigTemplate configTemplate;

    public EntityCreatedTemplateWrapper(ConfigTemplate configTemplate) {
        this.configTemplate = configTemplate;
    }

    public ConfigTemplate getConfigTemplate() {
        return configTemplate;
    }
}
