package com.siemens.cto.aem.service.dispatch.impl;

import org.springframework.integration.Message;

import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.service.dispatch.DispatchNotificationService;

public class JvmControlCompletionService {

    private final DispatchNotificationService<Jvm> notificationService;
    
    public JvmControlCompletionService(DispatchNotificationService<Jvm> theNotificationService) {
        notificationService = theNotificationService;
    }
    
    public void completeControl(Message<? extends JvmDispatchCommand> dispatchMessage) {
        // log completion
        
        // ... then notify
        JvmDispatchCommand jvmDispatchCommand = dispatchMessage.getPayload();
        Identifier<Jvm> jvmId = jvmDispatchCommand.getCommand().getJvmId();
        notificationService.notifyCompletion(jvmId);  
    }
}
