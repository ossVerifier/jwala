package com.siemens.cto.aem.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.service.resource.CreatedTemplateWrapper;

import java.util.List;

/**
 * Wrapper for templates created for grouped entities.
 *
 * Created by JC043760 on 4/13/2016.
 */
public class GroupedEntitiesCreatedTemplateWrapper implements CreatedTemplateWrapper {

    final private Group group;
    final private List<ConfigTemplate> configTemplateList;

    public GroupedEntitiesCreatedTemplateWrapper(final Group group, final List<ConfigTemplate> configTemplateList) {
        this.group = group;
        this.configTemplateList = configTemplateList;
    }

    public Group getGroup() {
        return group;
    }

    public List<ConfigTemplate> getConfigTemplateList() {
        return configTemplateList;
    }
}
