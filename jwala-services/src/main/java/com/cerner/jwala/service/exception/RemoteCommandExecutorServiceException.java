package com.cerner.jwala.service.exception;

/**
 * RuntimeException wrapper for {@link com.cerner.jwala.service.RemoteCommandExecutorService}
 *
 * Created by Jedd Cuison on 3/28/2016.
 */
public class RemoteCommandExecutorServiceException extends RuntimeException {

    public RemoteCommandExecutorServiceException(String s) {
        super(s);
    }

    public RemoteCommandExecutorServiceException(Throwable throwable) {
        super(throwable);
    }

}
