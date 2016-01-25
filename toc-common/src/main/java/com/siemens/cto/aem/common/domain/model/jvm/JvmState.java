package com.siemens.cto.aem.common.domain.model.jvm;

import com.siemens.cto.aem.common.domain.model.state.OperationalState;

import java.util.HashMap;
import java.util.Map;

/**
 * JvmState defines the known states for JVMs.
 * JVMs utilize infrastructure-provided code to send JVM state to TOC.
 * The translation is done using the enum names defined for JvmState here.
 * 
 * @author horspe00
 */
public enum JvmState implements OperationalState {

    JVM_NEW          (StateName.NEW, Started.NO),
    JVM_INITIALIZING (StateName.INITIALIZING, Started.YES),
    JVM_INITIALIZED  (StateName.INITIALIZING, Started.YES),
    JVM_START        (StateName.START_SENT, Started.YES) /* TODO: Remove from enum. This is no longer part of the JVM state. */ ,
    JVM_STARTING     (StateName.STARTING, Started.YES),
    JVM_STARTED      (StateName.STARTED, Started.YES),
    JVM_STOP         (StateName.STOP_SENT, Started.YES) /* TODO: Remove from enum. This is no longer part of the JVM state. */,
    JVM_STOPPING     (StateName.STOPPING, Started.YES),
    JVM_STOPPED      (StateName.STOPPED, Started.YES) /* Reported by ReportingLifeCycleListener.
                                                         This states that the application server has STOPPED which is
                                                         different from saying that the application itself has exited or
                                                         has been terminated. */,
    JVM_DESTROYING  (StateName.DESTROYING, Started.YES),
    JVM_DESTROYED   (StateName.DESTROYED, Started.YES),
    JVM_UNKNOWN     (StateName.UNKNOWN, Started.NO),
    JVM_FAILED      (StateName.FAILED, Started.NO),
    SVC_STOPPED     (StateName.STOPPED, Started.NO) /* Reported by something other than the ReportingLifeCycleListener
                                                        e.g. sc query. This means that Window's service states that the
                                                        application is no longer running. */,
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

    JvmState(final String stateName, final boolean startedFlag) {
        this.stateName = stateName;
        this.isStartedState = startedFlag;
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

    private static class StateName {
        public static final String INITIALIZING = "INITIALIZING";
        public static final String NEW = "NEW";
        public static final String START_SENT = "START SENT";
        public static final String STARTING = "STARTING";
        public static final String STARTED = "STARTED";
        public static final String STOP_SENT = "STOP SENT";
        public static final String STOPPING = "STOPPING";
        public static final String STOPPED = "STOPPED";
        public static final String DESTROYING = "DESTROYING";
        public static final String DESTROYED = "DESTROYED";
        public static final String UNKNOWN = "UNKNOWN";
        public static final String FAILED = "FAILED";
    }

    private static class Started {
        public static final boolean YES = true;
        public static final boolean NO = false;
    }

}
