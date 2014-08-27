package com.siemens.cto.aem.service.group.impl;


import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.FAILED;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.REACHABLE;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.START_REQUESTED;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.STOP_REQUESTED;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.UNKNOWN;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.UNREACHABLE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.CurrentGroupState.StateDetail;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.StateService;

/**
 * Instantaneous FSM for calculating group state at a particular time
 */
public class GroupStateManagerTableImpl implements GroupStateMachine {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateManagerTableImpl.class);

    public static class Node {
        private final Map<WebServerReachableState, GroupState> wsEdges;
        private final Map<JvmState, GroupState> jvmEdges;
        public Node(Map<WebServerReachableState, GroupState> wsEdges, Map<JvmState, GroupState> jvmEdges) {
            this.wsEdges = wsEdges;
            this.jvmEdges = jvmEdges;
        }
        public GroupState transit(JvmState sig) { return jvmEdges.get(sig); }
        public GroupState transit(WebServerReachableState sig) { return wsEdges.get(sig); }
    }
    public static class NodeBuilder {
        private Map<WebServerReachableState, GroupState> wsEdges = new ConcurrentHashMap<>();
        private Map<JvmState, GroupState> jvmEdges = new ConcurrentHashMap<>();

        public NodeBuilder() {}
        public NodeBuilder edge(JvmState signal, GroupState endpoint) {
            jvmEdges.put(signal, endpoint);
            return this;
        }
        public NodeBuilder edge(WebServerReachableState signal, GroupState endpoint) {
            wsEdges.put(signal, endpoint);
            return this;
        }
        public Node build() {
            return new Node(wsEdges, jvmEdges);
        }
    }
    private static final Map<GroupState, Node> ne = new ConcurrentHashMap<>();
    static {

        // Unqualified states are statically imported WebServerReachableStates (for brevity)

        ne.put(GroupState.UNKNOWN, new NodeBuilder().edge(JvmState.START_REQUESTED,   GroupState.STARTING)
                             .edge(START_REQUESTED,        GroupState.STARTING)
                             .edge(JvmState.STARTED,    GroupState.STARTED)
                             .edge(REACHABLE,       GroupState.STARTED)
                             .edge(JvmState.STOP_REQUESTED,   GroupState.STOPPING)
                             .edge(STOP_REQUESTED,        GroupState.STOPPING)
                             .edge(UNREACHABLE,     GroupState.STOPPED)
                             .edge(JvmState.STOPPED,    GroupState.STOPPED)
                             .edge(JvmState.FAILED,         GroupState.UNKNOWN) // these will always stay in
                             .edge(JvmState.INITIALIZED,    GroupState.UNKNOWN) // the same state, we do not
                             .edge(JvmState.UNKNOWN,        GroupState.UNKNOWN) // handle bad states.
                             .edge(UNKNOWN,                 GroupState.UNKNOWN) // Web Server states too
                             .edge(FAILED,                  GroupState.UNKNOWN)
                             .build());

        ne.put(GroupState.STARTING, new NodeBuilder().edge(JvmState.START_REQUESTED,  GroupState.STARTING)
                             .edge(START_REQUESTED,        GroupState.STARTING)
                             .edge(JvmState.STARTED,    GroupState.STARTING)    // per 8/20 stay in starting
                             .edge(REACHABLE,       GroupState.STARTING)        // per 8/20 stay in starting
                             .edge(JvmState.STOP_REQUESTED,   GroupState.PARTIAL)
                             .edge(STOP_REQUESTED,        GroupState.PARTIAL)
                             .edge(UNREACHABLE,     GroupState.PARTIAL)
                             .edge(JvmState.STOPPED,    GroupState.PARTIAL)
                             .edge(JvmState.FAILED,         GroupState.STARTING) // these will always stay in
                             .edge(JvmState.INITIALIZED,    GroupState.STARTING) // the same state, we do not
                             .edge(JvmState.UNKNOWN,        GroupState.STARTING) // handle bad states.
                             .edge(UNKNOWN,                 GroupState.STARTING) // Web Server states too
                             .edge(FAILED,                  GroupState.STARTING)
                             .build());

        ne.put(GroupState.STOPPING, new NodeBuilder().edge(JvmState.START_REQUESTED,  GroupState.PARTIAL)
                             .edge(START_REQUESTED,        GroupState.PARTIAL)
                             .edge(JvmState.STARTED,    GroupState.PARTIAL)
                             .edge(REACHABLE,       GroupState.PARTIAL)
                             .edge(JvmState.STOP_REQUESTED,   GroupState.STOPPING)
                             .edge(STOP_REQUESTED,        GroupState.STOPPING)
                             .edge(UNREACHABLE,     GroupState.STOPPING)         // per 8/20 stay in stopping
                             .edge(JvmState.STOPPED,    GroupState.STOPPING)     // per 8/20 stay in stopping
                             .edge(JvmState.FAILED,         GroupState.STOPPING) // these will always stay in
                             .edge(JvmState.INITIALIZED,    GroupState.STOPPING) // the same state, we do not
                             .edge(JvmState.UNKNOWN,        GroupState.STOPPING) // handle bad states.
                             .edge(UNKNOWN,                 GroupState.STOPPING) // Web Server states too
                             .edge(FAILED,                  GroupState.STOPPING) // Web Server states too
                             .build());

        ne.put(GroupState.STARTED, new NodeBuilder().edge(JvmState.START_REQUESTED,   GroupState.STARTING) // per 8/20 go to starting
                             .edge(START_REQUESTED,        GroupState.STARTING)   // per 8/20 go to starting
                             .edge(JvmState.STARTED,    GroupState.STARTED)
                             .edge(REACHABLE,       GroupState.STARTED)
                             .edge(JvmState.STOP_REQUESTED,   GroupState.STOPPING)// per 8/20 go to stopping
                             .edge(STOP_REQUESTED,        GroupState.STOPPING)    // per 8/20 go to stopping
                             .edge(UNREACHABLE,     GroupState.PARTIAL)
                             .edge(JvmState.STOPPED,    GroupState.PARTIAL)
                             .edge(JvmState.FAILED,         GroupState.STARTED) // these will always stay in
                             .edge(JvmState.INITIALIZED,    GroupState.STARTED) // the same state, we do not
                             .edge(JvmState.UNKNOWN,        GroupState.STARTED) // handle bad states.
                             .edge(UNKNOWN,                 GroupState.STARTED) // Web Server states too
                             .edge(FAILED,                  GroupState.STARTED) // Web Server states too
                             .build());

        ne.put(GroupState.STOPPED, new NodeBuilder().edge(JvmState.START_REQUESTED,   GroupState.STARTING) // per 8/20 go to starting
                             .edge(START_REQUESTED,        GroupState.STARTING) // per 8/20 go to starting
                             .edge(JvmState.STARTED,    GroupState.PARTIAL)
                             .edge(REACHABLE,       GroupState.PARTIAL)
                             .edge(JvmState.STOP_REQUESTED,   GroupState.STOPPING) // per 8/20 go to stopping
                             .edge(STOP_REQUESTED,        GroupState.STOPPING)   // per 8/20 go to stopping
                             .edge(UNREACHABLE,     GroupState.STOPPED)
                             .edge(JvmState.STOPPED,    GroupState.STOPPED)
                             .edge(JvmState.FAILED,         GroupState.STOPPED) // these will always stay in
                             .edge(JvmState.INITIALIZED,    GroupState.STOPPED) // the same state, we do not
                             .edge(JvmState.UNKNOWN,        GroupState.STOPPED) // handle bad states.
                             .edge(UNKNOWN,                 GroupState.STOPPED) // Web Server states too
                             .edge(FAILED,                  GroupState.STOPPED) // Web Server states too
                             .build());

        ne.put(GroupState.PARTIAL, new NodeBuilder().edge(JvmState.START_REQUESTED,   GroupState.PARTIAL)
                             .edge(START_REQUESTED,        GroupState.PARTIAL)
                             .edge(JvmState.STARTED,    GroupState.PARTIAL)
                             .edge(REACHABLE,       GroupState.PARTIAL)
                             .edge(JvmState.STOP_REQUESTED,   GroupState.PARTIAL)
                             .edge(STOP_REQUESTED,        GroupState.PARTIAL)
                             .edge(UNREACHABLE,     GroupState.PARTIAL)
                             .edge(JvmState.STOPPED,    GroupState.PARTIAL)
                             .edge(JvmState.FAILED,         GroupState.PARTIAL) // these will always stay in
                             .edge(JvmState.INITIALIZED,    GroupState.PARTIAL) // the same state, we do not
                             .edge(JvmState.UNKNOWN,        GroupState.PARTIAL) // handle bad states.
                             .edge(UNKNOWN,                 GroupState.PARTIAL) // Web Server states too
                             .edge(FAILED,                  GroupState.PARTIAL) // Web Server states too
                             .build());

    }


    // ========================   Attributes    ==============================

    private Group currentGroup;
    private CurrentGroupState currentGroupState;
    private ConcurrentHashMap<Enum<?>, AtomicInteger> counters = new ConcurrentHashMap<>();

    // ========================   USES Beans    ==============================

    @Autowired
    GroupPersistenceService groupPersistenceService;

    @Autowired
    @Qualifier("jvmStatePersistenceService")
    StatePersistenceService<Jvm, JvmState> jvmStatePersistenceService;

    @Autowired
    WebServerDao        webServerDao;

    @Autowired
    @Qualifier("webServerStateService")
    StateService<WebServer, WebServerReachableState>    webServerStateService;

    /**
     * State transition and counting of jvms and ws active
     * Note that null states coming back from the database
     * are treated as UNKNOWN.
     */
    private synchronized CurrentGroupState refreshState(Group group) {
        currentGroupState = null;
        GroupState state = GroupState.UNKNOWN;

        for(AtomicInteger stateCount : counters.values()) {
            stateCount.set(0);
        }

        List<WebServer> webServers = webServerDao.findWebServersBelongingTo(group.getId(), PaginationParameter.all());

        if(!webServers.isEmpty()) {

            Set<Identifier<WebServer>> webServerSet = new HashSet<>();
            for(WebServer webServer : webServers) {
                webServerSet.add(webServer.getId());
            }
            Set<CurrentState<WebServer,WebServerReachableState>> results = webServerStateService.getCurrentStates(webServerSet);

            for(CurrentState<WebServer, WebServerReachableState> wsState : results) {
                WebServerReachableState value = wsState!=null?wsState.getState():WebServerReachableState.UNKNOWN;

                counters.get(value).incrementAndGet();
                state = ne.get(state).transit(value);
            }
        }

        for(Jvm jvm : group.getJvms()) {
            CurrentState<Jvm, JvmState> jvmState = jvmStatePersistenceService.getState(jvm.getId());
            JvmState value = jvmState!=null?jvmState.getState():JvmState.UNKNOWN;

            counters.get(value).incrementAndGet();
            state = ne.get(state).transit(value);
        }

        int jvmTotal = 0;
        int wsTotal = 0;
        for(JvmState eachJvmState : JvmState.values()) {
            jvmTotal += counters.get(eachJvmState).get();
        }
        for(WebServerReachableState eachWsState : WebServerReachableState.values()) {
            wsTotal += counters.get(eachWsState).get();
        }
        int jvmStarted = counters.get(JvmState.STARTED).get();
        int wsStarted = counters.get(WebServerReachableState.REACHABLE).get();

        return currentGroupState = newState(state, jvmStarted, jvmTotal, wsStarted, wsTotal);
    }

    private CurrentGroupState newState(GroupState state, int jvmStarted, int jvmTotal, int wsStarted, int wsTotal) {
        currentGroupState = new CurrentGroupState(currentGroup.getId(), state,DateTime.now(),
                new StateDetail(jvmStarted, jvmTotal),
                new StateDetail(wsStarted, wsTotal)
                );

        logCurrentState();
        return currentGroupState;
    }

    private CurrentGroupState newState(GroupState state) {
        currentGroupState = new CurrentGroupState(currentGroup.getId(), state,DateTime.now(),
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
        for(JvmState eachJvmState : JvmState.values()) {
            counters.put(eachJvmState, new AtomicInteger(0));
        }
        for(WebServerReachableState eachWsState : WebServerReachableState.values()) {
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
        return newState(GroupState.STOPPING);
    }

    @Override
    @Transactional
    public CurrentGroupState signalStartRequested(User user) {
        return newState(GroupState.STARTING);
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
        return currentGroupState.getState() != GroupState.STARTED;
    }

    @Override
    @Transactional
    public boolean canStop() {
        return currentGroupState.getState() != GroupState.STOPPED;
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
        if(currentGroup == null) {
            return super.toString();
        } else {
            return "gsm:{id="+currentGroup.getId().getId()+",name='"+currentGroup.getName()+"',state="+currentGroupState+"}";
        }
    }
}
