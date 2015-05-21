package com.siemens.cto.aem.service.group.impl;


import static com.siemens.cto.aem.domain.model.jvm.JvmState.*;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.WS_FAILED;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.WS_REACHABLE;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.WS_STARTING;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.WS_STOPPING;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.WS_UNKNOWN;
import static com.siemens.cto.aem.domain.model.webserver.WebServerReachableState.WS_UNREACHABLE;

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

        ne.put(GroupState.GRP_UNKNOWN, new NodeBuilder()
                             .edge(WS_STARTING,    GroupState.GRP_STARTING)
                             .edge(JVM_STARTING,   GroupState.GRP_STARTING)
                             .edge(JVM_STARTED,    GroupState.GRP_STARTED)
                             .edge(WS_REACHABLE,   GroupState.GRP_STARTED)
                             .edge(JVM_STOPPING,   GroupState.GRP_STOPPING)
                             .edge(WS_STOPPING,    GroupState.GRP_STOPPING)
                             .edge(WS_UNREACHABLE, GroupState.GRP_STOPPED)
                             .edge(JVM_STOPPED,    GroupState.GRP_STOPPING)
                             .edge(SVC_STOPPED,    GroupState.GRP_STOPPED)
                             .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                             .edge(JVM_INITIALIZED,GroupState.GRP_STARTING)
                             .edge(JVM_NEW,        GroupState.GRP_STARTING)
                             .edge(JVM_INITIALIZING,GroupState.GRP_STARTING)
                             .edge(JVM_START,      GroupState.GRP_STARTING)
                             .edge(JVM_STOP,       GroupState.GRP_STOPPING)
                             .edge(JVM_DESTROYING, GroupState.GRP_UNKNOWN)
                             .edge(JVM_DESTROYED,  GroupState.GRP_UNKNOWN)
                             .edge(JVM_STALE,      GroupState.GRP_UNKNOWN) 
                             .edge(JVM_UNKNOWN,    GroupState.GRP_UNKNOWN) // handle bad states.
                             .edge(WS_UNKNOWN,     GroupState.GRP_UNKNOWN) 
                             .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                             .build());

        ne.put(GroupState.GRP_STARTING, new NodeBuilder()
                             .edge(JVM_STARTING,   GroupState.GRP_STARTING)
                             .edge(WS_STARTING,    GroupState.GRP_STARTING)
                             .edge(JVM_STARTED,    GroupState.GRP_STARTING)    // per 8/20 stay in starting
                             .edge(WS_REACHABLE,   GroupState.GRP_STARTING)        // per 8/20 stay in starting
                             .edge(JVM_STOPPING,   GroupState.GRP_PARTIAL)
                             .edge(WS_STOPPING,    GroupState.GRP_PARTIAL)
                             .edge(WS_UNREACHABLE, GroupState.GRP_PARTIAL)
                             .edge(JVM_STOPPED,    GroupState.GRP_PARTIAL)
                             .edge(SVC_STOPPED,    GroupState.GRP_PARTIAL)
                             .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                             .edge(JVM_INITIALIZED,GroupState.GRP_STARTING)
                             .edge(JVM_UNKNOWN,    GroupState.GRP_STARTING)
                             .edge(WS_UNKNOWN,     GroupState.GRP_STARTING)
                             .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                             .edge(JVM_NEW,        GroupState.GRP_STARTING)
                             .edge(JVM_INITIALIZING,GroupState.GRP_STARTING)
                             .edge(JVM_START,      GroupState.GRP_STARTING)
                             .edge(JVM_STOP,       GroupState.GRP_STARTING)
                             .edge(JVM_DESTROYING,  GroupState.GRP_STARTING)
                             .edge(JVM_DESTROYED,  GroupState.GRP_STARTING)
                             .edge(JVM_STALE,      GroupState.GRP_STARTING) 
                             .build());

        ne.put(GroupState.GRP_STOPPING, new NodeBuilder()
                             .edge(JVM_STARTING,   GroupState.GRP_PARTIAL)
                             .edge(WS_STARTING,    GroupState.GRP_PARTIAL)
                             .edge(JVM_STARTED,    GroupState.GRP_PARTIAL)
                             .edge(WS_REACHABLE,   GroupState.GRP_PARTIAL)
                             .edge(JVM_STOPPING,   GroupState.GRP_STOPPING)
                             .edge(WS_STOPPING,    GroupState.GRP_STOPPING)
                             .edge(WS_UNREACHABLE, GroupState.GRP_STOPPING)         // per 8/20 stay in stopping
                             .edge(JVM_STOPPED,    GroupState.GRP_STOPPING)     // per 8/20 stay in stopping
                             .edge(SVC_STOPPED,    GroupState.GRP_STOPPING)
                             .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                             .edge(JVM_INITIALIZED,GroupState.GRP_STOPPING)
                             .edge(JVM_UNKNOWN,    GroupState.GRP_STOPPING)
                             .edge(WS_UNKNOWN,     GroupState.GRP_STOPPING)
                             .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                             .edge(JVM_NEW,        GroupState.GRP_STOPPING)
                             .edge(JVM_INITIALIZING,GroupState.GRP_STOPPING)
                             .edge(JVM_START,      GroupState.GRP_STOPPING)
                             .edge(JVM_STOP,       GroupState.GRP_STOPPING)
                             .edge(JVM_DESTROYING,  GroupState.GRP_STOPPING)
                             .edge(JVM_DESTROYED,  GroupState.GRP_STOPPING)
                             .edge(JVM_STALE,      GroupState.GRP_STOPPING) 
                             .build());

        ne.put(GroupState.GRP_STARTED, new NodeBuilder()
                             .edge(JVM_STARTING,   GroupState.GRP_STARTING) // per 8/20 go to starting
                             .edge(WS_STARTING,    GroupState.GRP_STARTING)   // per 8/20 go to starting
                             .edge(JVM_STARTED,    GroupState.GRP_STARTED)
                             .edge(WS_REACHABLE,   GroupState.GRP_STARTED)
                             .edge(JVM_STOPPING,   GroupState.GRP_STOPPING)// per 8/20 go to stopping
                             .edge(WS_STOPPING,    GroupState.GRP_STOPPING)    // per 8/20 go to stopping
                             .edge(WS_UNREACHABLE, GroupState.GRP_PARTIAL)
                             .edge(JVM_STOPPED,    GroupState.GRP_STOPPING)
                             .edge(SVC_STOPPED,    GroupState.GRP_PARTIAL)
                             .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                             .edge(JVM_INITIALIZED,GroupState.GRP_STARTING)
                             .edge(JVM_UNKNOWN,    GroupState.GRP_STARTED)
                             .edge(WS_UNKNOWN,     GroupState.GRP_STARTED)
                             .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                             .edge(JVM_NEW,        GroupState.GRP_STARTING)
                             .edge(JVM_INITIALIZING,GroupState.GRP_STARTING)
                             .edge(JVM_START,      GroupState.GRP_STARTED)
                             .edge(JVM_STOP,       GroupState.GRP_STOPPING)
                             .edge(JVM_DESTROYING,  GroupState.GRP_STARTED)
                             .edge(JVM_DESTROYED,  GroupState.GRP_STARTED)
                             .edge(JVM_STALE,      GroupState.GRP_STARTED)                              .build());

        ne.put(GroupState.GRP_STOPPED, new NodeBuilder()
                             .edge(JVM_STARTING,   GroupState.GRP_STARTING) // per 8/20 go to starting
                             .edge(WS_STARTING,    GroupState.GRP_STARTING) // per 8/20 go to starting
                             .edge(JVM_STARTED,    GroupState.GRP_PARTIAL)
                             .edge(WS_REACHABLE,   GroupState.GRP_PARTIAL)
                             .edge(JVM_STOPPING,   GroupState.GRP_STOPPING) // per 8/20 go to stopping
                             .edge(WS_STOPPING,    GroupState.GRP_STOPPING)   // per 8/20 go to stopping
                             .edge(WS_UNREACHABLE, GroupState.GRP_STOPPED)
                             .edge(JVM_STOPPED,    GroupState.GRP_STOPPING)
                             .edge(SVC_STOPPED,    GroupState.GRP_STOPPED)
                             .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                             .edge(JVM_INITIALIZED,GroupState.GRP_STARTING)
                             .edge(JVM_UNKNOWN,    GroupState.GRP_STOPPED)
                             .edge(WS_UNKNOWN,     GroupState.GRP_STOPPED)
                             .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                             .edge(JVM_NEW,        GroupState.GRP_STARTING)
                             .edge(JVM_INITIALIZING,GroupState.GRP_STARTING)
                             .edge(JVM_START,      GroupState.GRP_STARTING)
                             .edge(JVM_STOP,       GroupState.GRP_STOPPED)
                             .edge(JVM_DESTROYING,  GroupState.GRP_STOPPED)
                             .edge(JVM_DESTROYED,  GroupState.GRP_STOPPED)
                             .edge(JVM_STALE,      GroupState.GRP_STOPPED)                              
                             .build());

        ne.put(GroupState.GRP_PARTIAL, new NodeBuilder()
                             .edge(JVM_STARTING,   GroupState.GRP_PARTIAL)
                             .edge(WS_STARTING,    GroupState.GRP_PARTIAL)
                             .edge(JVM_STARTED,    GroupState.GRP_PARTIAL)
                             .edge(WS_REACHABLE,   GroupState.GRP_PARTIAL)
                             .edge(JVM_STOPPING,   GroupState.GRP_PARTIAL)
                             .edge(WS_STOPPING,    GroupState.GRP_PARTIAL)
                             .edge(WS_UNREACHABLE, GroupState.GRP_PARTIAL)
                             .edge(JVM_STOPPED,    GroupState.GRP_PARTIAL)
                             .edge(SVC_STOPPED,    GroupState.GRP_PARTIAL)
                             .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                             .edge(JVM_INITIALIZED,GroupState.GRP_PARTIAL)
                             .edge(JVM_UNKNOWN,    GroupState.GRP_PARTIAL)
                             .edge(WS_UNKNOWN,     GroupState.GRP_PARTIAL)
                             .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                             .edge(JVM_NEW,        GroupState.GRP_PARTIAL)
                             .edge(JVM_INITIALIZING,GroupState.GRP_PARTIAL)
                             .edge(JVM_START,      GroupState.GRP_PARTIAL)
                             .edge(JVM_STOP,       GroupState.GRP_PARTIAL)
                             .edge(JVM_DESTROYING,  GroupState.GRP_PARTIAL)
                             .edge(JVM_DESTROYED,  GroupState.GRP_PARTIAL)
                             .edge(JVM_STALE,      GroupState.GRP_PARTIAL)   
                             .build());

        ne.put(GroupState.GRP_FAILURE, new NodeBuilder()
                            .edge(JVM_STARTING,   GroupState.GRP_FAILURE)
                            .edge(WS_STARTING,    GroupState.GRP_FAILURE)
                            .edge(JVM_STARTED,    GroupState.GRP_FAILURE)
                            .edge(WS_REACHABLE,   GroupState.GRP_FAILURE)
                            .edge(JVM_STOPPING,   GroupState.GRP_FAILURE)
                            .edge(WS_STOPPING,    GroupState.GRP_FAILURE)
                            .edge(WS_UNREACHABLE, GroupState.GRP_FAILURE)
                            .edge(JVM_STOPPED,    GroupState.GRP_FAILURE)
                            .edge(SVC_STOPPED,    GroupState.GRP_FAILURE)
                            .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                            .edge(JVM_INITIALIZED,GroupState.GRP_FAILURE)
                            .edge(JVM_UNKNOWN,    GroupState.GRP_FAILURE)
                            .edge(WS_UNKNOWN,     GroupState.GRP_FAILURE)
                            .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                            .edge(JVM_NEW,        GroupState.GRP_FAILURE)
                            .edge(JVM_INITIALIZING,GroupState.GRP_FAILURE)
                            .edge(JVM_START,      GroupState.GRP_FAILURE)
                            .edge(JVM_STOP,       GroupState.GRP_FAILURE)
                            .edge(JVM_DESTROYING,  GroupState.GRP_FAILURE)
                            .edge(JVM_DESTROYED,  GroupState.GRP_FAILURE)
                            .edge(JVM_STALE,      GroupState.GRP_FAILURE)   
                            .build());

        ne.put(GroupState.GRP_INITIALIZED, new NodeBuilder()
                            .edge(JVM_STARTING,   GroupState.GRP_STARTING)
                            .edge(WS_STARTING,    GroupState.GRP_STARTING)
                            .edge(JVM_STARTED,    GroupState.GRP_STARTED)
                            .edge(WS_REACHABLE,   GroupState.GRP_STARTED)
                            .edge(JVM_STOPPING,   GroupState.GRP_STOPPING)
                            .edge(WS_STOPPING,    GroupState.GRP_STOPPING)
                            .edge(WS_UNREACHABLE, GroupState.GRP_STOPPED)
                            .edge(JVM_STOPPED,    GroupState.GRP_STOPPING)
                            .edge(SVC_STOPPED,    GroupState.GRP_STOPPED)
                            .edge(JVM_FAILED,     GroupState.GRP_FAILURE)
                            .edge(JVM_INITIALIZED,GroupState.GRP_STARTING)
                            .edge(JVM_UNKNOWN,    GroupState.GRP_UNKNOWN)
                            .edge(WS_UNKNOWN,     GroupState.GRP_UNKNOWN)
                            .edge(WS_FAILED,      GroupState.GRP_FAILURE)
                            .edge(JVM_NEW,        GroupState.GRP_STARTING)
                            .edge(JVM_INITIALIZING,GroupState.GRP_STARTING)
                            .edge(JVM_START,      GroupState.GRP_STARTING)
                            .edge(JVM_STOP,       GroupState.GRP_STOPPING)
                            .edge(JVM_DESTROYING,  GroupState.GRP_STOPPING)
                            .edge(JVM_DESTROYED,  GroupState.GRP_STOPPING)
                            .edge(JVM_STALE,      GroupState.GRP_UNKNOWN)
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
     * are treated as JVM_UNKNOWN.
     */
    private synchronized CurrentGroupState refreshState(Group group) {
        currentGroupState = null;
        GroupState state = GroupState.GRP_UNKNOWN;

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
                WebServerReachableState value = wsState!=null?wsState.getState():WebServerReachableState.WS_UNKNOWN;

                counters.get(value).incrementAndGet();
                state = ne.get(state).transit(value);
            }
        }

        for(Jvm jvm : group.getJvms()) {
            CurrentState<Jvm, JvmState> jvmState = jvmStatePersistenceService.getState(jvm.getId());
            JvmState value = jvmState!=null?jvmState.getState():JvmState.JVM_UNKNOWN;

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
        int jvmStarted = counters.get(JvmState.JVM_STARTED).get();
        int wsStarted = counters.get(WebServerReachableState.WS_REACHABLE).get();

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
        return newState(GroupState.GRP_STOPPING);
    }

    @Override
    @Transactional
    public CurrentGroupState signalStartRequested(User user) {
        return newState(GroupState.GRP_STARTING);
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
        return currentGroupState.getState() != GroupState.GRP_STARTED;
    }

    @Override
    @Transactional
    public boolean canStop() {
        return currentGroupState.getState() != GroupState.GRP_STOPPED;
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
