package com.cerner.jwala.service.resource;

import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.exception.ResourceServiceException;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Outlines what a concrete resource handler should look like and what it can do.
 * This abstract class is also the corner stone for implementing a chain or responsibility pattern.
 *
 * Created by JC043760 on 7/21/2016
 */
public abstract class ResourceHandler {

    protected ResourceDao resourceDao;
    protected ResourceHandler successor;

    public abstract ConfigTemplate fetchResource(ResourceIdentifier resourceIdentifier);
    public abstract CreateResourceResponseWrapper createResource(ResourceIdentifier resourceIdentifier,
                                                                 ResourceTemplateMetaData metaData,
                                                                 InputStream data);
    public abstract void deleteResource(ResourceIdentifier resourceIdentifier);

    protected abstract boolean canHandle(ResourceIdentifier resourceIdentifier);

    protected String convertResourceTemplateMetaDataToJson(final ResourceTemplateMetaData resourceTemplateMetaData) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(resourceTemplateMetaData);
        } catch (final IOException ioe) {
            throw new ResourceServiceException(ioe);
        }
    }
}
