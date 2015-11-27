package com.siemens.cto.aem.domain.model.jvm;

import com.siemens.cto.aem.domain.model.state.OperationalState;

import java.util.HashMap;
import java.util.Map;

/**
 * JvmState defines the known states for JVMs.
 * 
 * JVMs utilize infrastructure-provided code to send 
 * JVM state to TOC. 
 * 
 * The translation is done using the enum names
 * defined for JvmState here. 
 * 
 * @author horspe00
 *
 */
public enum JvmState implements OperationalState {

    JVM_NEW         ("NEW",_NOT_A_STARTED_STATE),
    JVM_INITIALIZING("INITIALIZING",_IS_A_STARTED_STATE),
    JVM_INITIALIZED ("INITIALIZED",_IS_A_STARTED_STATE),
    JVM_START       ("START SENT",_IS_A_STARTED_STATE),
    JVM_STARTING    ("STARTING",_IS_A_STARTED_STATE),
    JVM_STARTED     ("STARTED",_IS_A_STARTED_STATE),
    JVM_STOP        ("STOP SENT",_IS_A_STARTED_STATE),
    JVM_STOPPING    ("STOPPING",_IS_A_STARTED_STATE),
    JVM_STOPPED     ("SHUTTING DOWN",_IS_A_STARTED_STATE),
    JVM_DESTROYING  ("DESTROYING",_IS_A_STARTED_STATE),
    JVM_DESTROYED   ("DESTROYED",_IS_A_STARTED_STATE),
    JVM_UNKNOWN     ("UNKNOWN",_NOT_A_STARTED_STATE),
    JVM_FAILED      ("FAILED",_NOT_A_STARTED_STATE),
    SVC_STOPPED     ("STOPPED",_NOT_A_STARTED_STATE),
    ;

    private static final Map<String, JvmState> LOOKUP_MAP = new HashMap<>();
    private boolean isStartedState;
    
    static {
        for (final JvmState state : values()) {
            LOOKUP_MAP.put(state.toPersistentString(), state);
        }
    }

    public static JvmState convertFrom(final String aStateName) {
        if (LOOKUP_MAP.containsKey(aStateName)) {
            return LOOKUP_MAP.get(aStateName);
        }
        return JVM_UNKNOWN;
    }

    private final String stateName;

    private JvmState(final String theStateName) {
        stateName = theStateName;
    }
    private JvmState(final String theStateName, boolean theIsStartedFlag) {
        stateName = theStateName;
        this.isStartedState = theIsStartedFlag;
    }

    @Override
    public String toStateString() {
        return stateName;
    }

    @Override
    public String toPersistentString() {
        return name();
    }

    public boolean isStartedState() {
        return isStartedState;
    }

}
