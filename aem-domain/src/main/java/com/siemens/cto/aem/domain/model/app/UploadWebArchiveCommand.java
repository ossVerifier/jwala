package com.siemens.cto.aem.domain.model.app;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.command.Command;

public class UploadWebArchiveCommand implements Serializable, Command {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused") // TODO 
    transient private ByteArrayInputStream uploadedFile;
    
    public UploadWebArchiveCommand(Application app, String string, ByteArrayInputStream uploadedFile, AuditEvent now) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void validateCommand() throws BadRequestException {
        // TODO Auto-generated method stub        
    }

}
