package com.cerner.jwala.common.domain.model.resource;

import java.io.IOException;

/**
 * Exception wrapper for {@link ResourceTemplateMetaData}
 *
 * Created by JC043760 on 10/4/2016.
 */
public class ResourceTemplateMetaDataException extends RuntimeException {

    public ResourceTemplateMetaDataException(final String msg, final IOException e) {
        super(msg, e);
    }
}
