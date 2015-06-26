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

    JVM_NEW         ("NEW"),
    JVM_INITIALIZING("INITIALIZING"),
    JVM_INITIALIZED ("INITIALIZED"),
    JVM_START       ("START SENT"),
    JVM_STARTING    ("STARTING"),
    JVM_STARTED     ("STARTED"),
    JVM_STOP        ("STOP SENT"),
    JVM_STOPPING    ("STOPPING"),
    JVM_STOPPED     ("SHUTTING DOWN"),
    JVM_DESTROYING  ("DESTROYING"),
    JVM_DESTROYED   ("DESTROYED"),
    JVM_UNKNOWN     ("UNKNOWN"),
    JVM_STALE       ("NO HEARTBEAT"),
    JVM_FAILED      ("FAILED"),
    SVC_STOPPED     ("STOPPED")
    ;

    private static final Map<String, JvmState> LOOKUP_MAP = new HashMap<>();

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

    @Override
    public String toStateString() {
        return stateName;
    }

    @Override
    public String toPersistentString() {
        return name();
    }

}
