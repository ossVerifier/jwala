package com.siemens.cto.aem.domain.command.app;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.model.app.Application;

import java.io.Serializable;

public class RemoveWebArchiveCommand implements Serializable, Command {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final Application application;
        
    public RemoveWebArchiveCommand(final Application application) {
        this.application = application;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        // intentionally empty
    }

    public Application getApplication() {
        return application;
    }
    
}
