package com.siemens.cto.aem.request.dispatch;


/**
 * 
 * Defines the state of dispatched commands
 * 
 */
public enum DispatchCommandStatus {
    
    QUEUED,
    EXEC_LOCAL,
    EXEC_REMOTE, 
    COMPLETED;
    
}
