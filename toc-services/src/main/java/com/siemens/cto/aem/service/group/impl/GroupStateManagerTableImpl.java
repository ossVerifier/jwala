package com.siemens.cto.aem.service.group.impl;


import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState.StateDetail;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.StateService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.siemens.cto.aem.common.domain.model.group.GroupState.*;
import static com.siemens.cto.aem.common.domain.model.jvm.JvmState.*;
import static com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState.*;

/**
 * Instantaneous FSM for calculating group state at a particular time
 */
public class GroupStateManagerTableImpl implements GroupStateMachine {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateManagerTableImpl.class);

    static class Node {
        final Map<OperationalState, GroupState> edges;

        public Node(Map<OperationalState, GroupState> edges) {
            this.edges = edges;
        }

        public GroupState transit(OperationalState sig) {
            return edges.get(sig);
        }
    }

    static class NodeBuilder {
        private Map<OperationalState, GroupState> edges = new ConcurrentHashMap<>();

        public NodeBuilder() {
        }

        public NodeBuilder edge(OperationalState signal, GroupState endpoint) {
            edges.put(signal, endpoint);
            return this;
        }

        public NodeBuilder edges(Set<OperationalState> transitions, GroupState endpoint) {
            for (OperationalState state : transitions) {
                edges.put(state, endpoint);
            }
            return this;
        }

        public Node build() {
            return new Node(edges);
        }
    }

    private static final Map<GroupState, Node> FSM = new ConcurrentHashMap<>();

    static {
        configureFsm();
        LOGGER.info(outputFsm());
    }

    /**
     * Build the FSM machine
     */
    private static synchronized void configureFsm() {
        // Create some temporary buckets of similar states, to simplify 
        // initialing the state machine.

        // Starting states
        Set<OperationalState> startingStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{
                JVM_NEW,
                JVM_INITIALIZING,
                JVM_INITIALIZED,
                JVM_START,
                JVM_STARTING,
                JVM_STARTED,
                WS_REACHABLE,
                WS_START_SENT
        }) {
            startingStates.add(state);
        }

        // Stopping states
        Set<OperationalState> stoppingStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{
                JVM_STOP,
                JVM_STOPPING,
                JVM_STOPPED,
                JVM_DESTROYING,
                JVM_DESTROYED,
                SVC_STOPPED,
                WS_UNREACHABLE,
                WS_STOP_SENT
        }) {
            stoppingStates.add(state);
        }

        // Failing states
        Set<OperationalState> failingStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{
                JVM_FAILED,
                WS_FAILED,
        }) {
            failingStates.add(state);
        }

        // Unknown states
        Set<OperationalState> unknownStates = new TreeSet<>(new OperationalState.OSComparator());
        for (OperationalState state : new OperationalState[]{
                JVM_UNKNOWN, /* was reused for group state ? */
                JVM_UNKNOWN,
                WS_UNKNOWN
        }) {
            unknownStates.add(state);
        }

        // Begin state machine initialization
        FSM.put(GRP_UNKNOWN, new NodeBuilder()
                .edges(startingStates, GRP_STARTING)
                .edges(stoppingStates, GRP_STOPPING)
                .edges(failingStates, GRP_FAILURE)
                .edges(unknownStates, GRP_UNKNOWN)
                .edge(JVM_STARTED, GRP_STARTED) // override
                .edge(WS_REACHABLE, GRP_STARTED) // override
                .edge(SVC_STOPPED, GRP_STOPPED) // override
                .edge(WS_UNREACHABLE, GRP_STOPPED) // override
                .build());

        FSM.put(GRP_INITIALIZED, FSM.get(GRP_UNKNOWN));

        FSM.put(GRP_STARTING, new NodeBuilder()
                .edges(startingStates, GRP_STARTING)
                .edges(stoppingStates, GRP_PARTIAL)
                .edges(failingStates, GRP_FAILURE)
                .edges(unknownStates, GRP_PARTIAL)
                .build());

        FSM.put(GRP_STOPPING, new NodeBuilder()
                .edges(startingStates, GRP_PARTIAL)
                .edges(stoppingStates, GRP_STOPPING)
                .edges(failingStates, GRP_FAILURE)
                .edges(unknownStates, GRP_PARTIAL)
                .build());

        FSM.put(GRP_STARTED, new NodeBuilder()
                .edges(startingStates, GRP_STARTING)
                .edges(stoppingStates, GRP_PARTIAL)
                .edges(failingStates, GRP_FAILURE)
                .edges(unknownStates, GRP_PARTIAL)
                .edge(JVM_STARTED, GRP_STARTED) // override
                .edge(WS_REACHABLE, GRP_STARTED) // override
                .build());

        FSM.put(GRP_STOPPED, new NodeBuilder()
                .edges(startingStates, GRP_PARTIAL)
                .edges(stoppingStates, GRP_STOPPING)
                .edges(failingStates, GRP_FAILURE)
                .edges(unknownStates, GRP_PARTIAL)
                .edge(SVC_STOPPED, GRP_STOPPED) // override
                .edge(WS_UNREACHABLE, GRP_STOPPED) // override
                .build());

        FSM.put(GRP_PARTIAL, new NodeBuilder()
                .edges(startingStates, GRP_PARTIAL)
                .edges(stoppingStates, GRP_PARTIAL)
                .edges(failingStates, GRP_FAILURE)
                .edges(unknownStates, GRP_PARTIAL)
                .build());

        FSM.put(GRP_FAILURE, new NodeBuilder()
                .edges(startingStates, GRP_FAILURE)
                .edges(stoppingStates, GRP_FAILURE)
                .edges(failingStates, GRP_FAILURE)
                .edges(unknownStates, GRP_FAILURE)
                .build());
    }

    /**
     * Prepare a message for display/logging.
     */
    public static String outputFsm() {
        StringBuilder fsmText = new StringBuilder();
        fsmText.append("Group FSM initialized with state machine: ");
        String comma1 = "";
        for (Entry<GroupState, Node> node : FSM.entrySet()) {
            fsmText.append(comma1);
            fsmText.append(node.getKey().name() + ": {");
            comma1 = ", ";

            String comma = "";
            for (Entry<OperationalState, GroupState> transition : node.getValue().edges.entrySet()) {
                fsmText.append(comma);
                fsmText.append(transition.getKey().toPersistentString());
                fsmText.append("=>");
                fsmText.append(transition.getValue().toPersistentString());
                comma = ", ";
            }
            fsmText.append("}");
        }
        return fsmText.toString();
    }

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
                state = FSM.get(state).transit(value);
            }
        }

        for (Jvm jvm : group.getJvms()) {
            CurrentState<Jvm, JvmState> jvmState = jvmStatePersistenceService.getState(jvm.getId());
            JvmState value = jvmState != null ? jvmState.getState() : JvmState.JVM_UNKNOWN;

            counters.get(value).incrementAndGet();
            state = FSM.get(state).transit(value);
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
    // =============== Constructor =====================

    public GroupStateManagerTableImpl() {
        for (JvmState eachJvmState : JvmState.values()) {
            counters.put(eachJvmState, new AtomicInteger(0));
        }
        for (WebServerReachableState eachWsState : WebServerReachableState.values()) {
            counters.put(eachWsState, new AtomicInteger(0));
        }
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
    public CurrentGroupState signalReset(User user) {
        return refreshState(currentGroup);
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
    public boolean refreshState() {
        refreshState(currentGroup);
        return true;
    }

    @Override
    @Transactional
    public void jvmError(Identifier<Jvm> jvmId) {
        refreshState(currentGroup);
    }

    @Override
    @Transactional
    public void jvmStopped(Identifier<Jvm> jvmId) {
        refreshState(currentGroup);
    }

    @Override
    @Transactional
    public void jvmStarted(Identifier<Jvm> jvmId) {
        refreshState(currentGroup);
    }

    @Override
    @Transactional
    public void wsError(Identifier<WebServer> wsId) {
        refreshState(currentGroup);
    }

    @Override
    @Transactional
    public void wsReachable(Identifier<WebServer> wsId) {
        refreshState(currentGroup);
    }

    @Override
    @Transactional
    public void wsUnreachable(Identifier<WebServer> wsId) {
        refreshState(currentGroup);
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
