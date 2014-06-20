package com.siemens.cto.aem.service.dispatch.impl;

import org.springframework.integration.Message;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;

public class JvmCommandExecutorBeanImpl {
    
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JvmCommandExecutorBeanImpl.class);

    public Message<? extends DispatchCommand>  deploy(Message<? extends DispatchCommand> msg) { 
        
        LOGGER.info("Would execute: " + msg.getPayload().toString());
        return msg;
    }
    

}
