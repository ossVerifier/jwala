package com.siemens.cto.aem.service.group.impl;


//import static com.siemens.cto.aem.domain.model.group.GroupState.ERROR;
//import static com.siemens.cto.aem.domain.model.group.GroupState.INITIALIZED;
//import static com.siemens.cto.aem.domain.model.group.GroupState.PARTIAL;
//import static com.siemens.cto.aem.domain.model.group.GroupState.STARTED;
//import static com.siemens.cto.aem.domain.model.group.GroupState.STARTING;
//import static com.siemens.cto.aem.domain.model.group.GroupState.STOPPED;
//import static com.siemens.cto.aem.domain.model.group.GroupState.STOPPING;
//import static com.siemens.cto.aem.domain.model.group.GroupState.UNKNOWN;
//import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StartCondition.CANNOT_START;
//import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StartCondition.CAN_START;
//import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StopCondition.CANNOT_STOP;
//import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StopCondition.CAN_STOP;
//
//import java.util.Deque;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedDeque;
//
//import org.joda.time.DateTime;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.expression.EvaluationContext;
//import org.springframework.expression.Expression;
//import org.springframework.expression.ExpressionParser;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//
//import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
//import com.siemens.cto.aem.domain.model.group.CurrentGroupState.StateDetail;
//import com.siemens.cto.aem.domain.model.group.Group;
//import com.siemens.cto.aem.domain.model.group.GroupState;
//import com.siemens.cto.aem.domain.model.id.Identifier;
//import com.siemens.cto.aem.domain.model.jvm.Jvm;
//import com.siemens.cto.aem.domain.model.jvm.JvmState;
//import com.siemens.cto.aem.domain.model.state.CurrentState;
//import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
//import com.siemens.cto.aem.domain.model.temporary.User;
//import com.siemens.cto.aem.domain.model.webserver.WebServer;
//import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
//import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
//import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
//import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
//import com.siemens.cto.aem.service.group.GroupStateMachine;
//import com.siemens.cto.aem.service.state.StateService;

/**
 * FSM built using spEL for handlers (Spring Expression Language)
 */
public class GroupStateManagerTableImpl extends GroupStateManagerImpl {
}
//public class GroupStateManagerTableImpl implements GroupStateMachine {
//
//    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateManagerTableImpl.class);
//
//    // Define what 'null' means for StateEntries
//    public static final String DEFAULT_STATE_IN_TRANSITION_EXPRESSION = "#enteringState";
//    public static final String DEFAULT_STATE_OUT_TRANSITION_EXPRESSION = "defaultStateOutTransitionHandler(#exitingState)";
//    public static final String DEFAULT_STATE_EXPRESSION = "defaultStateHandler(#state)";
//    public static final String NO_OP="currentState";
//
//    // Define what 'null' means for a return value from a state transition handler
//    public static final GroupState CONTINUE = null;
//
//    // Conditions - properties of being in a state
//    enum StartCondition { 
//        CAN_START,
//        CANNOT_START
//    }
//    
//    enum StopCondition { 
//        CAN_STOP,
//        CANNOT_STOP
//    }
//
//    enum Signal { 
//        REACH_WS_SIGNAL, 
//        UNREACH_WS_SIGNAL, 
//        ERROR_WS_SIGNAL,
//        START_JVM_SIGNAL, 
//        STOP_JVM_SIGNAL, 
//        ERROR_JVM_SIGNAL 
//    };
//    
//    // Keep a list of JVMs that triggered state updates - used for cached periodic FSM handling
//    private class Triggers {
//        private Map<Signal, Deque<Identifier<?>>> signals = new ConcurrentHashMap<>();
//
//        public boolean anySignalled(Signal... sigList) {
//            for(Signal s : sigList) { 
//                Deque<Identifier<?>> result = signals.get(s);
//                if(result != null && result.size() > 0) return true;
//            }
//            return false;
//        }
//
//        public void addSignal(Signal s, Identifier<?> id) {
//            Deque<Identifier<?>> result = signals.get(s);
//            if(result == null) { 
//                result = new ConcurrentLinkedDeque<>();
//                result.add(id);
//                signals.put(s, result);
//            } else { 
//                result.add(id);
//            }
//        }        
//        
//        public void drain() {
//            for(Deque<Identifier<?>> sigqueue: signals.values()) {
//                sigqueue.clear();
//            }
//        }
//    }
//    
//    // Variables For reporting state detail
//    private CurrentGroupState.StateDetail   jvmsDetail;
//    private CurrentGroupState.StateDetail   webServersDetail;
//    private DateTime                        lastChange;
//
//    // Tracking state changes internally during a state transition
//    private Group               currentGroup;
//    private GroupState          currentState;
//
//    // Internal implementation
//    private Triggers            triggers = new Triggers();
//    private EvaluationContext   context;
//    private static final User   SYSTEM_USER;
//    private static final Map<GroupState, StateEntry> STATES;
//    
//    // =========== HANDLER INJECTED BEANS ===================
//    @Autowired
//    GroupPersistenceService groupPersistenceService;
//
//    @Autowired
//    @Qualifier("jvmStatePersistenceService")
//    StatePersistenceService<Jvm, JvmState> jvmStatePersistenceService;
//
//    @Autowired
//    WebServerDao        webServerDao;
//
//    @Autowired
//    StateService<WebServer, WebServerReachableState>    webServerStateService;
//
//    
//    // =========== INITIALIZERS =============================
//    static { 
//        SYSTEM_USER = User.getSystemUser();
//
//        STATES = new HashMap<>();
//        ExpressionParser parser = new SpelExpressionParser();
//
//        STATES.put(null,           new StateEntry(parser, CANNOT_START,CANNOT_STOP, null,        NO_OP,      NO_OP,      NO_OP));
//        STATES.put(UNKNOWN,        new StateEntry(parser, CANNOT_START,CANNOT_STOP, null,        NO_OP,      NO_OP,      NO_OP));
//        STATES.put(ERROR,          new StateEntry(parser, CANNOT_START,CANNOT_STOP, INITIALIZED, null,      null,       null));
//        STATES.put(INITIALIZED,    new StateEntry(parser, CAN_START,   CAN_STOP,    INITIALIZED, "onInitializeIn()", "onInitializeIn()", null));
//        STATES.put(PARTIAL,        new StateEntry(parser, CAN_START,   CAN_STOP,    INITIALIZED, null, "onPartial()",    null));
//        STATES.put(STARTING,       new StateEntry(parser, CANNOT_START,CANNOT_STOP, INITIALIZED, null, "onStarting()",   null));
//        STATES.put(STARTED,        new StateEntry(parser, CANNOT_START,CAN_STOP,    INITIALIZED, null, "onStarted()",    null));
//        STATES.put(STOPPING,       new StateEntry(parser, CANNOT_START,CANNOT_STOP, INITIALIZED, null, "onStopping()",   null));
//        STATES.put(STOPPED,        new StateEntry(parser, CAN_START,   CANNOT_STOP, INITIALIZED, null, "onStopped()",    null));
//
//    }
//    // =========== CONSTRUCTOR ===================
//
//    public GroupStateManagerTableImpl() {
//        context = new StandardEvaluationContext(this);
//        jvmsDetail = new StateDetail(0, 0);
//        webServersDetail = new StateDetail(0, 0);
//    }
//
//    // =========== STATE HANDLERS ===========================
//    // Note: all state handler methods must be public
//    // But should not be invoked directly.
//
//    /**
//     * This is the DEFAULT_STATE_IN_TRANSITION_HANDLER
//     * Currently unused for testing
//    @Transactional
//    public GroupState defaultStateInTransitionHandler(GroupState enteringState) {
//        // use this to persist the transition to the Group table.
//        if(currentGroup.getCurrentState().getState() != enteringState) {
//
//            debug(this, "PERSIST", enteringState);
//
//            currentGroup = groupPersistenceService.updateGroupStatus(
//                    Event.create(new SetGroupStateCommand(currentGroup.getId(), enteringState), AuditEvent.now(currentUser)));
//        }
//        return CONTINUE;
//    }
//     * @return
//     */
//
//    /**
//     * This is the DEFAULT_STATE_OUT_TRANSITION_HANDLER
//     * @return
//     */
//    public GroupState defaultStateOutTransitionHandler(GroupState exitingState) {
//        return CONTINUE;
//    }
//
//    /**
//     * This is the DEFAULT_STATE_HANDLER
//     * @return
//     */
//    public GroupState defaultStateHandler(GroupState state) {
//        return CONTINUE;
//    }
//
//    /**
//     * Responsible for going from initialized to a real state
//     * @return
//     */
//    public GroupState onInitializeIn() {
//        // check with injected persistence service for number of active jvms to decide which state this
//        // should really be in.
//        // then call         defaultStateInTransitionHandler(); or persist.
//        return readPerceivedState();
//    }
//
//    /**
//     * Starting state, immediate
//     * @return
//     */
//    public GroupState onStarting() {
//        GroupState state = readPerceivedState();
//        if(state == GroupState.STARTED) {
//            return GroupState.STARTED;
//        }
//
//        if(state == GroupState.PARTIAL) {
//            if(triggers.anySignalled(Signal.START_JVM_SIGNAL, Signal.STOP_JVM_SIGNAL, Signal.REACH_WS_SIGNAL, Signal.UNREACH_WS_SIGNAL)) {
//                return GroupState.PARTIAL;
//            } else if(triggers.anySignalled(Signal.ERROR_JVM_SIGNAL, Signal.ERROR_WS_SIGNAL)) {
//                return GroupState.ERROR;
//            }
//        }
//        
//        return GroupState.STARTING;
//    }
//
//
//    /**
//     * Stopping state, immediate
//     * @return
//     */
//    public GroupState onStopping() {
//        GroupState state = readPerceivedState();
//        if(state == GroupState.STOPPED) {
//            return GroupState.STOPPED;
//        }
//        
//
//        if(state == GroupState.PARTIAL) {
//            if(triggers.anySignalled(Signal.START_JVM_SIGNAL, Signal.STOP_JVM_SIGNAL, Signal.REACH_WS_SIGNAL, Signal.UNREACH_WS_SIGNAL)) {
//                return GroupState.PARTIAL;
//            } else if(triggers.anySignalled(Signal.ERROR_JVM_SIGNAL, Signal.ERROR_WS_SIGNAL)) {
//                return GroupState.ERROR;
//            }
//        }
//        
//        return GroupState.STOPPING;
//    }
//
//
//    /**
//     * Stay Started
//     * @return
//     */
//    public GroupState onStarted() {
//        return readPerceivedState();
//    }
//
//    /**
//     * Stay Stopped
//     * @return
//     */
//    public GroupState onStopped() {
//        return readPerceivedState();
//    }
//
//    /**
//     * Stay Partial
//     * @return
//     */
//    public GroupState onPartial() {
//        return readPerceivedState();
//    }
//
//    // =========== STATE HELPERS ============================
//
//    private GroupState readPerceivedState() {
//
//        // Consider that we might be a long lived instance, so reload.
//        Group group = groupPersistenceService.getGroup(getCurrentGroup().getId());
//        
//        // Some of the servers in question may actually be in a STARTING or START REQUESTED state. 
//        // or stopping or stop requested state
//        // in these cases, this web server should return that specific state
//        // if that state is returned, then we should not transition to the state 
//        // identified by the other elements, but we should stay where we are.
//
//        GroupState jvmState = readPerceivedStateJvms(group);
//        GroupState webState = readPerceivedStateWebServers(group);
//
//        if(webState == GroupState.INITIALIZED) {
//            return jvmState;
//        }
//        if(jvmState == GroupState.INITIALIZED) {
//            return webState;
//        }
//
//        if(jvmState == GroupState.ERROR || webState == GroupState.ERROR) {
//            return GroupState.ERROR;
//        }
//        
//        if(jvmState.equals(GroupState.STARTING)
//                || jvmState.equals(GroupState.STOPPING)) {
//            return jvmState;
//        }
//
//        if(webState.equals(GroupState.STARTING)
//                || webState.equals(GroupState.STOPPING)) {
//            return webState;
//        }
//
//        if(!(jvmState.equals(webState))) { 
//            return GroupState.PARTIAL;
//        }
//
//        return jvmState; // both are the same at this point.
//    }
//
//    private GroupState readPerceivedStateJvms(Group group) { 
//        
//        int started = 0, unstarted = 0, errors = 0, starting = 0, stopping = 0;
//        for(Jvm jvm : group.getJvms()) {
//            CurrentState<Jvm, JvmState> jvmState = jvmStatePersistenceService.getState(jvm.getId());
//            if(jvmState == null) { 
//                ++unstarted;
//            } else {
//                switch(jvmState.getState()) {
//                case FAILED:
//                    break;
//                case INITIALIZED: 
//                case UNKNOWN:
//                    default: break;
//                    case START_REQUESTED:
//                        ++unstarted;
//                        ++starting;
//                        break;
//                    case STOP_REQUESTED:
//                        ++started;
//                        ++stopping;
//                        break;
//                    case STOPPED:
//                        ++unstarted;
//                        break;
//                    case STARTED:
//                        ++started;
//                        break;
//                
//                }
//            }
//        }
//
//        jvmsDetail.setStarted(started);
//        jvmsDetail.setTotal(unstarted + started);
//        
//        return progressToState(starting, stopping, started, unstarted, errors); 
//    }
//    
//    private GroupState progressToState(int starting, int stopping, int started, int unstarted, int errors) {
//        if(errors > 0) return GroupState.ERROR;
//        
//        if(starting > 0 && stopping == 0 && currentState == GroupState.STARTING) {
//            return GroupState.STARTING;
//        }
//       
//        if(stopping > 0 && starting == 0 && currentState == GroupState.STOPPING) {
//            return GroupState.STOPPING;
//        }
//        
//        if(started == 0 && unstarted == 0) { 
//            return GroupState.INITIALIZED;
//        } else if(started == 0) {
//            return GroupState.STOPPED;
//        } else if(started > 0 && unstarted > 0) {
//            return GroupState.PARTIAL;
//        } else {
//            return GroupState.STARTED;
//        }
//    }
//
//    private GroupState readPerceivedStateWebServers(Group group) {
//
//        int started = 0, unstarted = 0, errors = 0 /*unsupported for web servers*/, starting = 0, stopping = 0;
//
//        List<WebServer> webServers = webServerDao.findWebServersBelongingTo(group.getId(), PaginationParameter.all());
//
//        if(!webServers.isEmpty()) {
//
//            Set<Identifier<WebServer>> webServerSet = new HashSet<>();
//            for(WebServer webServer : webServers) {
//                webServerSet.add(webServer.getId());
//            }
//            Set<CurrentState<WebServer,WebServerReachableState>> results = webServerStateService.getCurrentStates(webServerSet);
//
//            for(CurrentState<WebServer, WebServerReachableState> wsState : results) {
//                switch(wsState.getState()) {
//                case UNKNOWN:
//                    break;
//                default: 
//                    break;
//                case START_REQUESTED:
//                    ++unstarted;
//                    ++starting;
//                    break;
//                case STOP_REQUESTED:
//                    ++started;
//                    ++stopping;
//                    break;
//                case UNREACHABLE:
//                    ++unstarted;
//                    break;
//                case REACHABLE:
//                    ++started;
//                    break;                
//                }
//            }
//        }
//
//        webServersDetail.setStarted(started);
//        webServersDetail.setTotal(unstarted + started);
//        
//        return progressToState(starting, stopping, started, unstarted, errors); 
//    }
//
//    // =========== STATE ENGINE =============================
//
//    /**
//     * Changes state. After a state is entered, it is 'IN'
//     * @param proposedState
//     */
//    private synchronized void handleState(GroupState proposedState, User user) {
//        GroupState nextState;
//
//        try {
//
//            if(proposedState == null) {
//                // do nothing
//                return;
//            }
//
//            nextState = proposedState;
//
//            if(proposedState == currentState) {
//                // staying in same state
//                nextState = (GroupState) STATES.get(currentState).state(context, this, currentState);
//                if(nextState == currentState) {
//                    return; // no change.
//                }
//            }
//
//            // Temporary variables
//            if(nextState == null) {
//                nextState = proposedState;
//            }
//            GroupState interimState = currentState;
//            GroupState nextState2 = nextState;
//
//            // while state changes required.
//            while(nextState != interimState) {
//                // Exit current state.
//                nextState2 = STATES.get(interimState).out(context,this, interimState);
//                // New state change proposed?
//                if(nextState2 != null) {
//                    nextState = nextState2;
//                }
//
//                // If we have somewhere to go ( on the first transition we do ),
//                if(nextState != null) {
//                    // Enter new state, record proposition
//                    nextState2 = STATES.get(nextState).in(context, this, nextState);
//                    // track current 'interim' state
//                    interimState = nextState;
//                    // new state change proposed ?
//                    if(nextState2 != null && nextState2 != nextState) {
//                        // Otherwise, yet another state transition?
//                        nextState = nextState2;
//                    }
//                    // else null or nextState = ok, so enter.
//                }
//            }
//            currentState = interimState;
//        } finally {
//            this.triggers.drain();
//        }
//    }
//
//    // ========== See Interface com.siemens.cto.aem.service.group.GroupStateMachine ================
//
//    @Override
//    public void initializeGroup(Group group, User user) {
//        currentState = null;
//        currentGroup = group;
//        lastChange = group.getCurrentState().getAsOf();
//
//        // invoke FSM for the first time. Should change currentState.
//        handleState(group.getCurrentState().getState() == null ? INITIALIZED : group.getCurrentState().getState(), user);
//    }
//
//    @Override
//    public CurrentGroupState signalReset(User user) {
//        handleState(STATES.get(currentState).resetState, user);
//        return getCurrentStateDetail();
//    }
//
//    @Override
//    public boolean canStart() {
//        return STATES.get(currentState).canStart == CAN_START;
//    }
//
//    @Override
//    public boolean canStop() {
//        return STATES.get(currentState).canStop == CAN_STOP;
//    }
//
//    @Override
//    public void jvmStarted(Identifier<Jvm> jvmId) {
//        triggers.addSignal(Signal.START_JVM_SIGNAL, jvmId);
//        handleState(currentState, SYSTEM_USER);
//    }
//
//    @Override
//    public void jvmStopped(Identifier<Jvm> jvmId) {
//        triggers.addSignal(Signal.STOP_JVM_SIGNAL, jvmId);
//        handleState(currentState, SYSTEM_USER);
//    }
//
//    @Override
//    public void jvmError(Identifier<Jvm> jvmId) {
//        triggers.addSignal(Signal.ERROR_JVM_SIGNAL, jvmId);
//        handleState(GroupState.ERROR, SYSTEM_USER);
//    }
//
//    @Override
//    public CurrentGroupState signalStartRequested(User user) {
//        handleState(GroupState.STARTING, user);
//        return getCurrentStateDetail();
//    }
//
//    @Override
//    public CurrentGroupState signalStopRequested(User user) {
//        handleState(GroupState.STOPPING, user);
//        return getCurrentStateDetail();
//    }
//
//    @Override
//    public GroupState getCurrentState() {
//        return currentState;
//    }
//
//    @Override
//    public CurrentGroupState getCurrentStateDetail() {
//        return new CurrentGroupState(currentGroup.getId(), currentState, lastChange, jvmsDetail, webServersDetail);
//    }
//
//    @Override
//    public Group getCurrentGroup() {
//        return currentGroup;
//    }
//
//    // ========== STATE MAP ENTRY HOLDER CLASS  ================
//
//
//    private static class StateEntry {
//        private static final String SPELVAR_CURRENT_STATE = "state";
//        private static final String SPELVAR_EXITING_STATE = "exitingState";
//        private static final String SPELVAR_ENTERING_STATE = "enteringState";
//        private StartCondition canStart;
//        private StopCondition canStop;
//        private GroupState   resetState;
//        private Expression   stateInExpression;
//        private Expression   stateExpression;
//        private Expression   stateOutExpression;
//        StateEntry(StartCondition canStart, StopCondition canStop, GroupState resetState) {
//            this.canStart = canStart;
//            this.canStop = canStop;
//            this.resetState = resetState;
//        }
//        StateEntry(ExpressionParser parser, StartCondition canStart, StopCondition canStop, GroupState resetState, String in, String stateHandler, String out) {
//            this(canStart, canStop, resetState);
//            this.stateInExpression = parser.parseExpression(in != null ? in : DEFAULT_STATE_IN_TRANSITION_EXPRESSION);
//            this.stateOutExpression = parser.parseExpression(out != null ? out : DEFAULT_STATE_OUT_TRANSITION_EXPRESSION);
//            this.stateExpression = parser.parseExpression(stateHandler != null ? stateHandler : DEFAULT_STATE_EXPRESSION);
//        }
//
//        GroupState in(EvaluationContext context, GroupStateManagerTableImpl fsm, GroupState enteringState) {
//            try {
//                debug(fsm, "ENTER", enteringState);
//                context.setVariable(SPELVAR_ENTERING_STATE, enteringState);
//                return this.stateInExpression.getValue(context, fsm, GroupState.class);
//            } finally {
//                context.setVariable(SPELVAR_ENTERING_STATE, null);
//            }
//        }
//        GroupState out(EvaluationContext context, GroupStateManagerTableImpl fsm, GroupState exitingState) {
//            try {
//                debug(fsm, "EXIT", exitingState);
//                context.setVariable(SPELVAR_EXITING_STATE, exitingState);
//                return this.stateOutExpression.getValue(context, fsm, GroupState.class);
//            } finally {
//                context.setVariable(SPELVAR_EXITING_STATE, null);
//            }
//        }
//        GroupState state(EvaluationContext context, GroupStateManagerTableImpl fsm, GroupState state) {
//            try {
//                context.setVariable(SPELVAR_CURRENT_STATE, state);
//                return this.stateExpression.getValue(context, fsm, GroupState.class);
//            } finally {
//                context.setVariable(SPELVAR_CURRENT_STATE, null);
//            }
//        }
//    }
//
//    // ==== Log Helper ====
//
//    static void debug(GroupStateManagerTableImpl fsm, String op, GroupState state) {
//        if(state != null) {
//            LOGGER.debug("Group FSM for id={} name='{}': {} {}", fsm.getCurrentGroup().getId().getId(), fsm.getCurrentGroup().getName(), op, state);
//        }
//    }
//
//    @Override
//    public void wsError(Identifier<WebServer> wsId) {
//        triggers.addSignal(Signal.ERROR_WS_SIGNAL, wsId);
//        handleState(GroupState.ERROR, SYSTEM_USER);        
//    }
//
//    @Override
//    public void wsReachable(Identifier<WebServer> wsId) {
//        triggers.addSignal(Signal.REACH_WS_SIGNAL, wsId);
//        handleState(currentState, SYSTEM_USER);        
//    }
//
//    @Override
//    public void wsUnreachable(Identifier<WebServer> wsId) {
//        triggers.addSignal(Signal.UNREACH_WS_SIGNAL, wsId);
//        handleState(currentState, SYSTEM_USER);        
//    }
//
//}
