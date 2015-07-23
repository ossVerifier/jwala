package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommand;
import org.springframework.integration.Message;

public class DeployCommandExecutorBeanImpl {
    
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DeployCommandExecutorBeanImpl.class);

    public Message<JvmDispatchCommand>  deploy(Message<JvmDispatchCommand> msg) { 
        
        LOGGER.info("Would execute jvm command" + msg.getPayload().toString());
        return msg;
    }
    

}
