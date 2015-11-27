package com.siemens.cto.aem.domain.command.dispatch;


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
