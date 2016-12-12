package com.cerner.jwala.common.request.app;

import java.io.InputStream;
import java.io.Serializable;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.ValidWebArchiveNameRule;
import com.cerner.jwala.common.rule.app.GoodStreamRule;

public class UploadWebArchiveRequest implements Serializable, Request {

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
    public void validate() {
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
