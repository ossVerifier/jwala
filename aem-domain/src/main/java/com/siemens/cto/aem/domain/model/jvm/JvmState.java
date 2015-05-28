package com.siemens.cto.aem.domain.model.jvm;

import java.util.HashMap;
import java.util.Map;

import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.Stability;
import com.siemens.cto.aem.domain.model.state.Transience;

import static com.siemens.cto.aem.domain.model.state.Stability.STABLE;
import static com.siemens.cto.aem.domain.model.state.Stability.UNSTABLE;
import static com.siemens.cto.aem.domain.model.state.Transience.PERMANENT;
import static com.siemens.cto.aem.domain.model.state.Transience.TRANSIENT;

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

    JVM_NEW         ("NEW",             PERMANENT, STABLE  ),
    JVM_INITIALIZING("INITIALIZING",    TRANSIENT, UNSTABLE),
    JVM_INITIALIZED ("INITIALIZED",     TRANSIENT, STABLE  ),
    JVM_START       ("START SENT",      TRANSIENT, UNSTABLE),
    JVM_STARTING    ("STARTING",        PERMANENT, UNSTABLE),
    JVM_STARTED     ("STARTED",         PERMANENT, STABLE  ),
    JVM_STOP        ("STOP SENT",       TRANSIENT, UNSTABLE),
    JVM_STOPPING    ("STOPPING",        TRANSIENT, UNSTABLE),
    JVM_STOPPED     ("SHUTTING DOWN",   TRANSIENT, STABLE  ),
    JVM_DESTROYING  ("DESTROYING",      TRANSIENT, UNSTABLE),
    JVM_DESTROYED   ("DESTROYED",       PERMANENT, STABLE  ),
    JVM_UNKNOWN     ("UNKNOWN",         PERMANENT, UNSTABLE),
    JVM_STALE       ("NO HEARTBEAT",    PERMANENT, STABLE),
    JVM_FAILED      ("FAILED",          PERMANENT, STABLE),
    SVC_STOPPED     ("STOPPED",         PERMANENT, STABLE)
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
    private final Transience transientState;

    private final Stability stableState;

    private JvmState(final String theStateName,
                     final Transience theTransientState,
                     final Stability theStableState) {
        stateName = theStateName;
        transientState = theTransientState;
        stableState = theStableState;
    }

    @Override
    public String toStateString() {
        return stateName;
    }

    @Override
    public String toPersistentString() {
        return name();
    }

    @Override
    public Transience getTransience() {
        return transientState;
    }

    @Override
    public Stability getStability() {
        return stableState;
    }
}
