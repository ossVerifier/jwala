package com.siemens.cto.aem.service.dispatch.impl;

import java.util.List;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;

import com.siemens.cto.aem.domain.model.dispatch.GroupDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.JvmDispatchCommandResult;
import com.siemens.cto.aem.service.group.GroupControlService;

public class GroupCommandCompletionMessageHandler implements MessageHandler {
    
    private final GroupControlService groupControlService;

    public GroupCommandCompletionMessageHandler(SubscribableChannel inputChannel, GroupControlService theGroupControlService) {
        inputChannel.subscribe(this);
        groupControlService = theGroupControlService;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        GroupDispatchCommand groupDispatchCommand = (GroupDispatchCommand) message.getHeaders().get("GroupDispatchCommand");
        
        groupControlService.dispatchCommandComplete(groupDispatchCommand, (List<JvmDispatchCommandResult>) message.getPayload());
    }
}
