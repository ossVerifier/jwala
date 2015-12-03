package com.siemens.cto.aem.request.app;

import com.siemens.cto.aem.request.Request;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;

import java.io.Serializable;

public class RemoveWebArchiveRequest implements Serializable, Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final Application application;
        
    public RemoveWebArchiveRequest(final Application application) {
        this.application = application;
    }

    @Override
    public void validate() throws BadRequestException {
        // intentionally empty
    }

    public Application getApplication() {
        return application;
    }
    
}
