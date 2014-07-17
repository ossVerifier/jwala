package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;


/**
 * Invoked in response to incoming state changes - jvm or web server
 */
public class GroupStateServiceImpl {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateServiceImpl.class);

    public GroupStateServiceImpl() {}

    public void stateUpdate(CurrentJvmState cjs) {
        
        LOGGER.debug("State Update Received");
        // lookup group
        // establish group state machine
        // update group state in database.
    }
    
    public void stateUpdate(Object o) {
        
    }
}
