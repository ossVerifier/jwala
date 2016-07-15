package com.siemens.cto.aem.template.exception;

import com.siemens.cto.aem.template.ResourceFileGenerator;

/**
 * Class wrapper for exceptions thrown from {@link ResourceFileGenerator}.
 *
 * Created by JC043760 on 7/15/2016.
 */
public class ResourceFileGeneratorException extends RuntimeException {

    public ResourceFileGeneratorException(final String s, final Throwable throwable) {
        super(s, throwable);
    }
}
