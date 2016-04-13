package com.siemens.cto.aem.service.resource.impl;

import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;

/**
 * A response object wrapper the object being the meta data of a successfully created resource template.
 *
 * Created by JC043760 on 4/13/2016.
 */
public class CreateResourceTemplateApplicationResponseWrapper {

    private final ConfigTemplate configTemplate;

    public CreateResourceTemplateApplicationResponseWrapper(ConfigTemplate configTemplate) {
        this.configTemplate = configTemplate;
    }

    public String getMetaData() {
        return configTemplate.getMetaData();
    }
}
