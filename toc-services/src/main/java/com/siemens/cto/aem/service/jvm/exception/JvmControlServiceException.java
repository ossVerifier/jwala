package com.siemens.cto.aem.service.jvm.exception;

import com.siemens.cto.aem.service.jvm.JvmControlService;

/**
 * Wrapper for {@link JvmControlService} errors
 *
 * Created by JC043760 on 9/20/2016.
 */
public class JvmControlServiceException extends RuntimeException {

    public JvmControlServiceException(final String msg) {
        super(msg);
    }
}
