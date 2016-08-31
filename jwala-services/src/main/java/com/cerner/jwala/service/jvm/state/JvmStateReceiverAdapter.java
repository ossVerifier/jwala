package com.cerner.jwala.service.jvm.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.service.jvm.JvmStateService;
import org.apache.catalina.LifecycleState;
import org.apache.commons.lang.IllegalClassException;
import org.apache.commons.lang3.StringUtils;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The listener for JGroup messages
 */
public class JvmStateReceiverAdapter extends ReceiverAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateReceiverAdapter.class);
    private static final String STATE_KEY = "STATE";
    private static final String ID_KEY = "ID";

    private final JvmStateService jvmStateService;

    private final static Map<LifecycleState, JvmState> LIFECYCLE_JWALA_JVM_STATE_REF_MAP = new HashMap<>();

    static {
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.DESTROYED, JvmState.JVM_STOPPED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.DESTROYING, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.FAILED, JvmState.JVM_FAILED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.INITIALIZED, JvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.INITIALIZING, JvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.MUST_DESTROY, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.MUST_STOP, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.NEW, JvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTED, JvmState.JVM_STARTED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTING, JvmState.JVM_STARTING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTING_PREP, JvmState.JVM_STARTING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPED, JvmState.JVM_STOPPED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPING, JvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPING_PREP, JvmState.JVM_STOPPING);
    }

    public JvmStateReceiverAdapter(final JvmStateService jvmStateService) {
        this.jvmStateService = jvmStateService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(Message jgroupMessage) {
        final Address src = jgroupMessage.getSrc();
        final Map<String, Object> serverInfoMap = (Map) jgroupMessage.getObject();
        final JvmState jvmState;
        final String jvmId;
        LOGGER.debug("Received JGroups JVM state message {} {}", src, serverInfoMap);

        LifecycleState lifecycleState = (LifecycleState) serverInfoMap.get(STATE_KEY);
        if (lifecycleState == null) {
            // Assume that the message came from the old JGroups listener
            LOGGER.info("The state key = {} was not found in the lifecycle state map. " +
                        "Jwala will proceed with the assumption that that the message came from the legacy " +
                        "JGroups reporting lifecycle listener.", STATE_KEY);

            Object legacyIdKey = null;
            Object legacyStateKey = null;
            try {
                // If this if from legacy JGroups reporting lifecycle listener, the key will be of type ReportingJmsMessageKey
                // found in the infrastructure provided jar which is provided by the container therefore we just iterate
                // trough the keys to try to look for the "STATE" string. Note: We don't want to use ReportingJmsMessageKey
                // to make this compatible to containers that do not have infrastructure provided jar hence the key
                // iteration.
                for (Object key: serverInfoMap.keySet()) {
                    if (key.toString().equalsIgnoreCase(ID_KEY)) {
                        legacyIdKey = key;
                    }

                    if (key.toString().equalsIgnoreCase(STATE_KEY)) {
                        legacyStateKey = key;
                    }

                    if (legacyIdKey != null && legacyStateKey != null) {
                        break;
                    }
                }

                if (legacyStateKey != null) {
                    jvmId = serverInfoMap.get(legacyIdKey).toString();
                    jvmState = JvmState.valueOf(serverInfoMap.get(legacyStateKey).toString());
                } else {
                    LOGGER.error("Failed to get the legacy key in {}! Cannot extract the lifecycle from the JGroups message.", serverInfoMap);
                    return;
                }
            } catch (final IllegalArgumentException e) {
                LOGGER.error("Cannot process legacy key = {} since it is not defined in JvmState enum!", legacyStateKey, e);
                return;
            }
        } else {
            jvmId = serverInfoMap.get(ID_KEY).toString();
            jvmState = LIFECYCLE_JWALA_JVM_STATE_REF_MAP.get(lifecycleState);
        }

        if (!JvmState.JVM_STOPPED.equals(jvmState)) {
            jvmStateService.updateState(new Identifier<Jvm>(jvmId), jvmState, StringUtils.EMPTY);
        }
    }

    @Override
    public void viewAccepted(View view) {
        LOGGER.debug("JGroups coordinator cluster VIEW: {}", view.toString());
    }
}
