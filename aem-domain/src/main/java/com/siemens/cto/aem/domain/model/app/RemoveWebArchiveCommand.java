package com.siemens.cto.aem.domain.model.app;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;

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
