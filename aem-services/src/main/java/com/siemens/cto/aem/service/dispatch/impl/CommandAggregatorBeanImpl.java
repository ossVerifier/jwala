package com.siemens.cto.aem.service.dispatch.impl;

import java.util.Collection;

import org.springframework.integration.Message;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;

public class CommandAggregatorBeanImpl {
    
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CommandAggregatorBeanImpl.class);

    public Message<? extends DispatchCommand> aggregate(Collection<Message<? extends DispatchCommand>> results) {
        
        LOGGER.info("Aggregating " + results.size() );
        return results.iterator().next();
    }

    // add callback registration logic for use in tests
}
