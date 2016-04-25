package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.service.jvm.JvmResourceFileService;

/**
 * Exception wrapper for {@link JvmResourceFileService} related errors.
 *
 * Created by JC043760 on 4/15/2016.
 */
public class JvmResourceFileServiceException extends RuntimeException {

    public JvmResourceFileServiceException(final String msg) {
        super(msg);
    }

    public JvmResourceFileServiceException(final Throwable throwable) {
        super(throwable);
    }
}
