package com.siemens.cto.aem.service.exception;

/**
 * RuntimeException wrapper for {@link com.siemens.cto.aem.service.RemoteCommandExecutorService}
 *
 * Created by JC043760 on 3/28/2016.
 */
public class RemoteCommandExecutorServiceException extends RuntimeException {

    public RemoteCommandExecutorServiceException(String s) {
        super(s);
    }

    public RemoteCommandExecutorServiceException(Throwable throwable) {
        super(throwable);
    }

}
