package com.siemens.cto.aem.domain.model.app;

import java.io.InputStream;
import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.ValidWebArchiveNameRule;
import com.siemens.cto.aem.domain.model.rule.app.GoodStreamRule;

public class UploadWebArchiveCommand implements Serializable, Command {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    transient private InputStream data;   
    private Application application;
    private String filename;
    private Integer length;
    
    
    public UploadWebArchiveCommand(Application application, String filename, Integer length, InputStream data) {
        this.application = application;
        this.filename = filename;
        this.length = length;
        this.data = data;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(
                new ValidWebArchiveNameRule(this.filename),
                new GoodStreamRule(this.data)).validate();
    }

    public Application getApplication() {
        return application;
    }
    
    public String getFilename() { 
        return filename; 
    }
    
    public Integer getLength() { 
        return length;
    }
    
    public InputStream getTransientData() { 
        return data;
    }
}
