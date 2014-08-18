package com.siemens.cto.aem.service.group.impl;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.ChannelInterceptor;
import org.springframework.integration.support.MessageBuilder;

public class MessageTimingChannelInterceptor implements ChannelInterceptor {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MessageTimingChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if(message != null && LOGGER.isTraceEnabled()) {
            Object lastEvent = message.getHeaders().get("lastEventMs");
            Object start = message.getHeaders().get("startMs");
            long curTime = System.currentTimeMillis();
            if(lastEvent != null && start != null) { 
                long intervalDuration = curTime - (Long)lastEvent;
                long duration = curTime - (Long)start;
                LOGGER.trace("TIMING: Started {}ms ago Action {}ms : {}", (Long)duration, (Long)intervalDuration, message.getPayload().toString());
            }
            return MessageBuilder.fromMessage(message).setHeaderIfAbsent("startMs", curTime).setHeader("lastEventMs", curTime).build();
        }        
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        return true;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        if(message != null && LOGGER.isTraceEnabled()) {
            Object lastEvent = message.getHeaders().get("lastEventMs");
            Object start = message.getHeaders().get("startMs");
            long curTime = System.currentTimeMillis();
            if(lastEvent != null && start != null) { 
                long intervalDuration = curTime - (Long)lastEvent;
                long duration = curTime - (Long)start;
                LOGGER.trace("TIMING: Started {}ms ago Waited {}ms : {}", (Long)duration, (Long)intervalDuration, message.getPayload().toString());
            }
            return MessageBuilder.fromMessage(message).setHeaderIfAbsent("startMs", curTime).setHeader("lastEventMs", curTime).build();
        }        
        return message;
    }

}
