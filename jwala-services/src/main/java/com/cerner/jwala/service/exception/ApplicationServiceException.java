package com.cerner.jwala.service.exception;

import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exception.MessageResponseStatus;

import java.util.Collection;

/**
 * Wrapper for exceptions that happens inside the {@link com.cerner.jwala.service.app.ApplicationService} implementation(s)
 *
 * Created by z003bpej on 9/16/2015
 */
public class ApplicationServiceException extends InternalErrorException {

    public ApplicationServiceException(final String msg) {
        super(null, msg);
    }

    public ApplicationServiceException(final MessageResponseStatus theMessageResponseStatus, final String theMessage,
                                       final Collection<String> entityDetailsCollection) {
        super(theMessageResponseStatus, theMessage, entityDetailsCollection);
    }
}
