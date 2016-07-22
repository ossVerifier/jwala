package com.siemens.cto.aem.service.resource;

import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.ResourceDao;

/**
 * Outlines what a concrete resource handler should look like and what it can do.
 * This abstract class is also the corner stone for implementing a chain or responsibility pattern.
 *
 * Note: This was not written as an interface due to the intention of making the class variables and some
 *       methods protected.
 *
 * Created by JC043760 on 7/21/2016
 */
public abstract class ResourceHandler {

    protected ResourceDao resourceDao;
    protected ResourceHandler successor;

    public abstract ConfigTemplate fetchResource(ResourceIdentifier resourceIdentifier);
    public abstract void deleteResource(ResourceIdentifier resourceIdentifier);

    protected abstract boolean canHandle(ResourceIdentifier resourceIdentifier);
}
