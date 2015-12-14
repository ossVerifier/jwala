package com.siemens.cto.aem.common.dispatch;


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
