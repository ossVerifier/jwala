package com.cerner.jwala.service.jvm.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.jvm.JvmStateService;
import org.apache.catalina.LifecycleState;
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
    private static final String NAME_KEY = "NAME";

    private final JvmStateService jvmStateService;
    private final JvmPersistenceService jvmPersistenceService;

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

    public JvmStateReceiverAdapter(final JvmStateService jvmStateService, final JvmPersistenceService jvmPersistenceService) {
        this.jvmStateService = jvmStateService;
        this.jvmPersistenceService = jvmPersistenceService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receive(Message jgroupMessage) {
        final Address src = jgroupMessage.getSrc();
        final Map serverInfoMap = (Map) jgroupMessage.getObject();
        final JvmState jvmState;
        String jvmId = null;

        LOGGER.debug("Received JGroups JVM state message {} {}", src, serverInfoMap);

        final LifecycleState lifecycleState = (LifecycleState) serverInfoMap.get(STATE_KEY);
        if (lifecycleState == null) {
            // Assume that the message came from the old JGroups listener
            LOGGER.info("The state key = {} was not found in the lifecycle state map. Proceeding with legacy JGroup message decoding.", STATE_KEY);
            try {
                // If the serverInfoMap is from legacy JGroups reporting lifecycle listener, the key will be of type
                // ReportingJmsMessageKey found in the reporting lifecycle jar which is provided by the container.
                // The said class might not always be present in the future therefore to avoid runtime errors as a result
                // of using ReportingJmsMessageKey here, we have to use the generic "valueOf" method of java.lang.Enum
                // to derive the key that will be used to get the value in serverInfoMap.
                final Enum enumSample = (Enum) serverInfoMap.keySet().iterator().next();
                final Object legacyIdKey = Enum.valueOf(enumSample.getDeclaringClass(), ID_KEY);
                final Object legacyStateKey = Enum.valueOf(enumSample.getDeclaringClass(), STATE_KEY);
                jvmId = serverInfoMap.get(legacyIdKey).toString();
                jvmState = JvmState.valueOf(serverInfoMap.get(legacyStateKey).toString());
            } catch (final RuntimeException e) { // Since the adapter is a critical component used to display the JVM state
                                                 // let's make sure to catch all possible runtime exceptions and log them
                LOGGER.error("Error processing legacy JGroup message!", e);
                return;
            }
        } else {
            jvmState = LIFECYCLE_JWALA_JVM_STATE_REF_MAP.get(lifecycleState);

            Object id = serverInfoMap.get(ID_KEY);
            if (id != null) {
                jvmId = (String) id;
            } else {
                final Object name = serverInfoMap.get(NAME_KEY);
                if (name != null) {
                    id = getJvmId((String) name);
                    if (id != null) {
                        jvmId = id.toString();
                    }
                }
            }
        }

        if (jvmId != null && !JvmState.JVM_STOPPED.equals(jvmState)) {
            jvmStateService.updateState(new Identifier<Jvm>(jvmId), jvmState, StringUtils.EMPTY);
        } else if (jvmId == null) {
            LOGGER.error("Jvm id is null! Cannot update JVM state.");
        }
    }

    /**
     * Get the JVM id
     * @param name the JVM name
     * @return the id
     */
    private Long getJvmId(final String name) {
        LOGGER.info("Retrieving JVM id with name = {}...", name);
        try {
            return jvmPersistenceService.getJvmId(name);
        } catch (final RuntimeException e) { // Since the adapter is a critical component used to display the JVM state
                                             // let's make sure to catch all possible runtime exceptions and log them
            LOGGER.error("Failed to retrieve the JVM id with the name {}! Cannot update the JVM state.", name, e);
            return null;
        }
    }

    @Override
    public void viewAccepted(View view) {
        LOGGER.debug("JGroups coordinator cluster VIEW: {}", view.toString());
    }
}
