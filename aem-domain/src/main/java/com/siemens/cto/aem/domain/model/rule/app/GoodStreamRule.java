package com.siemens.cto.aem.domain.model.rule.app;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.Rule;

public class GoodStreamRule implements Rule {

    private final static Logger LOGGER = LoggerFactory.getLogger(GoodStreamRule.class); 

    private InputStream streamToValidate; 
    
    public GoodStreamRule(final InputStream inputStream) {
        this.streamToValidate = inputStream;
    }

    @Override
    public boolean isValid() {
        if(streamToValidate == null) return false;
        try {
            if(streamToValidate.markSupported()) {
                streamToValidate.mark(1);
                int val = streamToValidate.read();
                streamToValidate.reset();
                return val != -1;
            } else {
                return streamToValidate.available() > 0;
            }
        } catch (IOException e) {
            LOGGER.trace("Passed stream will not validate", e);
            return false;
        }        
    }

    @Override
    public void validate() throws BadRequestException {

        if(!isValid()) {
            throw new BadRequestException(AemFaultType.BAD_STREAM, "Cannot read uploaded file data");
        }
        
    }

}
