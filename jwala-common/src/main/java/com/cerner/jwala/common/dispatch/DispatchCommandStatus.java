package com.cerner.jwala.common.dispatch;


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
