package com.siemens.cto.aem.service.app.impl;

/**
 * Exception for application deployment related errors.
 *
 * Created by z003bpej on 9/9/2015.
 */
public class DeployApplicationConfException extends RuntimeException {

    public DeployApplicationConfException(final String msg) {
        super(msg);
    }

    public DeployApplicationConfException(Throwable t) {
        super(t);
    }

}
