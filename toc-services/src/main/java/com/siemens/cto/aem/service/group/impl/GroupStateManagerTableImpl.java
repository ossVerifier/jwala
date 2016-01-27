package com.siemens.cto.aem.service.group.impl;


import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState.StateDetail;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.GroupFiniteStateMachine;
import com.siemens.cto.aem.service.state.StateService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.siemens.cto.aem.common.domain.model.group.GroupState.*;

/**
 * Instantaneous FSM for calculating group state at a particular time
 */
public class GroupStateManagerTableImpl implements GroupStateMachine {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateManagerTableImpl.class);

    // ========================   Attributes    ==============================

    private Group currentGroup;
    private CurrentGroupState currentGroupState;
    private Map<Enum<?>, AtomicInteger> counters = new ConcurrentHashMap<>();

    // ========================   USES Beans    ==============================

    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    @Qualifier("jvmStatePersistenceService")
    StatePersistenceService<Jvm, JvmState> jvmStatePersistenceService;

    @Autowired
    WebServerPersistenceService webServerPersistenceService;

    @Autowired
    @Qualifier("webServerStateService")
    StateService<WebServer, WebServerReachableState> webServerStateService;

    // =============== Constructor =====================

    public GroupStateManagerTableImpl() {
        for (JvmState eachJvmState : JvmState.values()) {
            counters.put(eachJvmState, new AtomicInteger(0));
        }
        for (WebServerReachableState eachWsState : WebServerReachableState.values()) {
            counters.put(eachWsState, new AtomicInteger(0));
        }
    }

    /**
     * State transition and counting of jvms and ws active
     * Note that null states coming back from the database
     * are treated as JVM_UNKNOWN.
     */
    private synchronized CurrentGroupState refreshState(Group group) {
        currentGroupState = null;
        GroupState state = GRP_UNKNOWN;

        for (AtomicInteger stateCount : counters.values()) {
            stateCount.set(0);
        }

        List<WebServer> webServers = webServerPersistenceService.findWebServersBelongingTo(group.getId());

        if (!webServers.isEmpty()) {

            Set<Identifier<WebServer>> webServerSet = new HashSet<>();
            for (WebServer webServer : webServers) {
                webServerSet.add(webServer.getId());
            }
            Set<CurrentState<WebServer, WebServerReachableState>> results = webServerStateService.getCurrentStates(webServerSet);

            for (CurrentState<WebServer, WebServerReachableState> wsState : results) {
                WebServerReachableState value = wsState != null ? wsState.getState() : WebServerReachableState.WS_UNKNOWN;

                counters.get(value).incrementAndGet();
                state = GroupFiniteStateMachine.getInstance().computeGroupState(state, value);
            }
        }

        for (Jvm jvm : group.getJvms()) {
            CurrentState<Jvm, JvmState> jvmState = jvmStatePersistenceService.getState(jvm.getId(), StateType.JVM);
            JvmState value = jvmState != null ? jvmState.getState() : JvmState.JVM_UNKNOWN;

            counters.get(value).incrementAndGet();
            state = GroupFiniteStateMachine.getInstance().computeGroupState(state, value);
        }

        int jvmTotal = 0;
        int wsTotal = 0;
        for (JvmState eachJvmState : JvmState.values()) {
            jvmTotal += counters.get(eachJvmState).get();
        }
        for (WebServerReachableState eachWsState : WebServerReachableState.values()) {
            wsTotal += counters.get(eachWsState).get();
        }
        int jvmStarted = counters.get(JvmState.JVM_STARTED).get();
        int wsStarted = counters.get(WebServerReachableState.WS_REACHABLE).get();

        currentGroupState = newState(state, jvmStarted, jvmTotal, wsStarted, wsTotal);
        return currentGroupState;
    }

    private CurrentGroupState newState(GroupState state, int jvmStarted, int jvmTotal, int wsStarted, int wsTotal) {
        currentGroupState = new CurrentGroupState(currentGroup.getId(), state, DateTime.now(),
                new StateDetail(jvmStarted, jvmTotal),
                new StateDetail(wsStarted, wsTotal)
        );

        logCurrentState();
        return currentGroupState;
    }

    private CurrentGroupState newState(GroupState state) {
        currentGroupState = new CurrentGroupState(currentGroup.getId(), state, DateTime.now(),
                currentGroup.getCurrentState().getJvmsDetail(),
                currentGroup.getCurrentState().getWebServersDetail()
        );
        logCurrentState();
        return currentGroupState;
    }

    private void logCurrentState() {
        LOGGER.debug("GSM State: {}", this);
    }

    // =============== API METHODS =====================

    @Override
    @Transactional
    public synchronized void synchronizedInitializeGroup(Group group, User user) {
        currentGroup = group;

        currentGroupState = refreshState(currentGroup);
    }

    @Override
    @Transactional
    public CurrentGroupState signalStopRequested(User user) {
        return newState(GRP_STOPPING);
    }

    @Override
    @Transactional
    public CurrentGroupState signalStartRequested(User user) {
        return newState(GRP_STARTING);
    }

    @Override
    @Transactional
    public boolean canStart() {
        return currentGroupState.getState() != GRP_STARTED;
    }

    @Override
    @Transactional
    public boolean canStop() {
        return currentGroupState.getState() != GRP_STOPPED;
    }

    @Override
    public GroupState getCurrentState() {
        return currentGroupState.getState();
    }

    @Override
    public Group getCurrentGroup() {
        return currentGroup;
    }

    @Override
    public CurrentGroupState getCurrentStateDetail() {
        return currentGroupState;
    }

    @Override
    public String toString() {
        if (currentGroup == null) {
            return super.toString();
        } else {
            return "gsm:{id=" + currentGroup.getId().getId() + ",name='" + currentGroup.getName() + "',state=" + currentGroupState + "}";
        }
    }

}
