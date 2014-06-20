package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.group.Group;

/**
 * Top level Spring Integration Message Payload for 
 * commands to be executed.  
 * 
 * @author horspe00
 *
 */
public class GroupDispatchCommand extends SplittableDispatchCommand {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Group group;
}
