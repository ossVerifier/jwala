package com.cerner.jwala.service.jvm.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.jvm.JvmStateService;
import org.apache.catalina.LifecycleState;
import org.apache.commons.lang3.StringUtils;
import javax.persistence.NoResultException;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.cxf.interceptor.LoggingMessage.ID_KEY;

/**
 * The listener for JGroup messages
 */
public class JvmStateReceiverAdapter extends ReceiverAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateReceiverAdapter.class);
    public static final String STATE_KEY = "STATE";
    public static final String NAME_KEY = "NAME";

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

        jvmState = LIFECYCLE_JWALA_JVM_STATE_REF_MAP.get(lifecycleState);


        Jvm jvm = null;
        Object id = serverInfoMap.get(ID_KEY);
        if (id != null) {
            jvmId = (String) id;
            jvm = getJvmById(id);
        } else {
            final Object name = serverInfoMap.get(NAME_KEY);
            if (name != null) {
                jvm = getJvmByName((String )name);
                id = getJvmId((String) name);
                if (id != null) {
                    jvmId = id.toString();
                }
            }
        }

        if (jvmId != null && !JvmState.JVM_STOPPED.equals(jvmState)) {
            jvmStateService.updateState(jvm, jvmState, StringUtils.EMPTY);
        } else if (jvmId == null) {
            LOGGER.error("Jvm id is null! Cannot update JVM state.");
        }
    }

    /**
     * Get the JVM id
     * @param name the JVM name
     * @return the id
     */
    private Jvm getJvmByName(final String name) {
        LOGGER.debug("Retrieving JVM id with name = {}...", name);
        try {
            return jvmPersistenceService.findJvmByExactName(name);
        } catch (NoResultException e) {
            LOGGER.warn("Received a notification from a jvm named {} but Jwala doesn't know about a Jvm " +
                    "with that name!!!  ", name);
        }
        return null;
    }

    private Jvm getJvmById(final Object id) {
        LOGGER.debug("Retrieving JVM id with id = {}...", id);
        try {
            return jvmPersistenceService.getJvm(new Identifier<Jvm>((long) id));
        } catch (NoResultException e) {
            LOGGER.warn("Received a notification from a jvm named {} but Jwala doesn't know about a Jvm " +
                    "with that name!!!  ", id);
        }
        return null;
    }

    /**
     * Get the JVM id
     * @param name the JVM name
     * @return the id
     */
    private Long getJvmId(final String name) {
        LOGGER.debug("Retrieving JVM id with name = {}...", name);
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
