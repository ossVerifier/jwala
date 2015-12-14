package com.siemens.cto.aem.common.request.app;

import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.ValidWebArchiveNameRule;
import com.siemens.cto.aem.common.rule.app.GoodStreamRule;

import java.io.InputStream;
import java.io.Serializable;

public class UploadWebArchiveRequest implements Serializable, Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    transient private InputStream data;   
    private Application application;
    private String filename;
    private Long length;
    
    
    public UploadWebArchiveRequest(Application application, String filename, Long length, InputStream data) {
        this.application = application;
        this.filename = filename;
        this.length = length;
        this.data = data;
    }

    @Override
    public void validate() throws BadRequestException {
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
    
    public Long getLength() { 
        return length;
    }
    
    public InputStream getTransientData() { 
        return data;
    }
}
