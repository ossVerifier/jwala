package com.siemens.cto.aem.service.group.impl;

import static com.siemens.cto.aem.domain.model.group.GroupState.ERROR;
import static com.siemens.cto.aem.domain.model.group.GroupState.INITIALIZED;
import static com.siemens.cto.aem.domain.model.group.GroupState.PARTIAL;
import static com.siemens.cto.aem.domain.model.group.GroupState.STARTED;
import static com.siemens.cto.aem.domain.model.group.GroupState.STARTING;
import static com.siemens.cto.aem.domain.model.group.GroupState.STOPPED;
import static com.siemens.cto.aem.domain.model.group.GroupState.STOPPING;
import static com.siemens.cto.aem.domain.model.group.GroupState.UNKNOWN;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.ERROR_JVM_SIGNAL;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.ERROR_WS_SIGNAL;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.REACH_WS_SIGNAL;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.START_JVM_SIGNAL;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.STOP_JVM_SIGNAL;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.UNREACH_WS_SIGNAL;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.RESET_REQUESTED;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.STOP_REQUESTED;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.Signal.START_REQUESTED;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.StartCondition.CANNOT_START;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.StartCondition.CAN_START;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.StopCondition.CANNOT_STOP;
import static com.siemens.cto.aem.service.group.impl.AbstractGroupStateManager.StopCondition.CAN_STOP;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.CurrentGroupState.StateDetail;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.group.GroupStateMachine;
import com.siemens.cto.aem.service.state.StateService;


public abstract class AbstractGroupStateManager extends AbstractStateManager<GroupState, Group, AbstractGroupStateManager.StartCondition, AbstractGroupStateManager.StopCondition> implements GroupStateMachine {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractGroupStateManager.class);

    // Conditions - properties of being in a state
    enum StartCondition { 
        CAN_START,
        CANNOT_START
    }
    
    enum StopCondition { 
        CAN_STOP,
        CANNOT_STOP
    }

    enum Signal { 
        REACH_WS_SIGNAL, 
        UNREACH_WS_SIGNAL, 
        ERROR_WS_SIGNAL,
        START_JVM_SIGNAL, 
        STOP_JVM_SIGNAL, 
        ERROR_JVM_SIGNAL,
        START_REQUESTED,
        STOP_REQUESTED,
        RESET_REQUESTED
    };
    
    // Variables For reporting state detail
    protected CurrentGroupState.StateDetail   jvmsDetail;
    protected CurrentGroupState.StateDetail   webServersDetail;

    // state machine
    private boolean isInitialized = false;
    private static final User               SYSTEM_USER;
    private static final Map<ExternalizableState, StateEntry<GroupState, Group, AbstractGroupStateManager.StartCondition, AbstractGroupStateManager.StopCondition>> STATES;
    private StateManagerAddonTriggers<Signal>       triggers = new StateManagerAddonTriggers<>();

    /**
     * The synchronization implements with the Semaphore stateLock
     * is placed in this class because not all state machines might 
     * want the behaviour; that is, that state transitions are 
     * discarded if another thread is also refreshing the state.
     * 
     * The Group state machine is refreshed each time a trigger
     * happens, and reads current state, not trigger information 
     * to transition. Therefore, a second state refresh call 
     * will cause no change, and can safely be discarded. 
     * 
     * @author horspe00
     *
     */
    private Semaphore stateLock = new Semaphore(0);


    // Typedef only, reduces template syntax garbage
    private static class GroupStateEntry extends StateEntry<GroupState, Group, AbstractGroupStateManager.StartCondition, AbstractGroupStateManager.StopCondition> {

        GroupStateEntry(ExpressionParser parser, StartCondition canStart, StopCondition canStop, GroupState resetState,
                String in, String stateHandler, String out) {
            super(parser, canStart, canStop, resetState, in, stateHandler, out);
        }
        
    }
    
    // =========== STATE TRANSITION TABLE =============================
    static { 
        SYSTEM_USER = User.getSystemUser();

        STATES = new HashMap<>();
        ExpressionParser parser = new SpelExpressionParser();

        STATES.put(null,           new GroupStateEntry(parser, CANNOT_START,CANNOT_STOP, null,        NO_OP,      NO_OP,      NO_OP));
        STATES.put(UNKNOWN,        new GroupStateEntry(parser, CANNOT_START,CANNOT_STOP, null,        NO_OP,      "INITIALIZED",      null));
        STATES.put(ERROR,          new GroupStateEntry(parser, CANNOT_START,CANNOT_STOP, INITIALIZED, null,      null,       null));
        STATES.put(INITIALIZED,    new GroupStateEntry(parser, CAN_START,   CAN_STOP,    INITIALIZED, "onInitializeIn()", "onInitializeIn()", null));
        STATES.put(PARTIAL,        new GroupStateEntry(parser, CAN_START,   CAN_STOP,    INITIALIZED, null, "onPartial()",    null));
        STATES.put(STARTING,       new GroupStateEntry(parser, CANNOT_START,CANNOT_STOP, INITIALIZED, null, "onStarting()",   null));
        STATES.put(STARTED,        new GroupStateEntry(parser, CANNOT_START,CAN_STOP,    INITIALIZED, null, "onStarted()",    null));
        STATES.put(STOPPING,       new GroupStateEntry(parser, CANNOT_START,CANNOT_STOP, INITIALIZED, null, "onStopping()",   null));
        STATES.put(STOPPED,        new GroupStateEntry(parser, CAN_START,   CANNOT_STOP, INITIALIZED, null, "onStopped()",    null));
    }
    
    public AbstractGroupStateManager() {
        super(STATES, GroupState.INITIALIZED);
        jvmsDetail = new StateDetail(0, 0);
        webServersDetail = new StateDetail(0, 0);
    }
    
    @Override
    public synchronized void synchronizedInitializeGroup(Group group, User user) {
        if(!isInitialized) {
            isInitialized = true;
            super.initialize(group, group.getId(), group.getCurrentState().getState(), group.getCurrentState().getAsOf(), user);
            stateLock.release(); // first permit
        }
    }

    // ================== helpers for GSM only ==============================

    @Override
    protected synchronized void synchronizedHandleState(GroupState proposedState, User user) {
        if(isInitialized) 
        {
            try {
                super.synchronizedHandleState(proposedState, user);
            } finally { 
                triggers.drain();
            }
        } // otherwise we will aggregate triggers indefinitely
    }

    @Override
    protected void debug(String op, GroupState state) {
        if(state != null) {
            LOGGER.debug("Group FSM for id={}: {} {} jvms:{}, webservers:{}, triggers: {}", getEntityId().getId(), op, state!=null?state:"(null state)", jvmsDetail, webServersDetail, triggers.toString());
        }
    }

    private void signalFromUser(Signal signal, GroupState proposedState, User user) {
        triggers.addSignal(signal, new Identifier<User>(-1L));
        if(proposedState != null) {
            synchronizedHandleState(proposedState, user);                
        }
    }

    private void signal(Signal errorWsSignal, Identifier<?> id) {
        triggers.addSignal(errorWsSignal, id);
    }

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
    
    // ================== state machine itself  ==============================
    
    /**
     * Responsible for going from initialized to a real state
     * @return
     */
    public GroupState onInitializeIn() {
        // check with injected persistence service for number of active jvms to decide which state this
        // should really be in.
        // then call         defaultStateInTransitionHandler(); or persist.
        return readPerceivedState();
    }

    /**
     * Starting state, immediate
     * @return
     */
    public GroupState onStarting() {
        GroupState state = readPerceivedState();
        if(state == GroupState.STARTED) {
            return GroupState.STARTED;
        }

        if(state == GroupState.PARTIAL) {
            if(triggers.anySignalled(Signal.START_JVM_SIGNAL, Signal.STOP_JVM_SIGNAL, Signal.REACH_WS_SIGNAL, Signal.UNREACH_WS_SIGNAL)) {
                return GroupState.PARTIAL;
            } else if(triggers.anySignalled(Signal.ERROR_JVM_SIGNAL, Signal.ERROR_WS_SIGNAL)) {
                return GroupState.ERROR;
            }
        }
        
        return GroupState.STARTING;
    }


    /**
     * Stopping state, immediate
     * @return
     */
    public GroupState onStopping() {
        GroupState state = readPerceivedState();
        if(state == GroupState.STOPPED) {
            return GroupState.STOPPED;
        }
        

        if(state == GroupState.PARTIAL) {
            if(triggers.anySignalled(Signal.START_JVM_SIGNAL, Signal.STOP_JVM_SIGNAL, Signal.REACH_WS_SIGNAL, Signal.UNREACH_WS_SIGNAL)) {
                return GroupState.PARTIAL;
            } else if(triggers.anySignalled(Signal.ERROR_JVM_SIGNAL, Signal.ERROR_WS_SIGNAL)) {
                return GroupState.ERROR;
            }
        }
        
        return GroupState.STOPPING;
    }


    /**
     * Stay Started
     * @return
     */
    public GroupState onStarted() {
        return readPerceivedState();
    }

    /**
     * Stay Stopped
     * @return
     */
    public GroupState onStopped() {
        return readPerceivedState();
    }

    /**
     * Stay Partial
     * @return
     */
    public GroupState onPartial() {
        return readPerceivedState();
    }

    // =========== STATE HELPERS ============================

    abstract GroupState readPerceivedState();
    abstract GroupState readPerceivedStateJvms(Group group); 
    abstract GroupState readPerceivedStateWebServers(Group group); 
    abstract GroupState progressToState(int starting, int stopping, int started, int unstarted, int errors);

    // ================== trigger methods below ==============================

    @Override
    public void wsError(Identifier<WebServer> wsId) {
        signal(ERROR_WS_SIGNAL, wsId);
    }


    @Override
    public void wsReachable(Identifier<WebServer> wsId) {
        signal(REACH_WS_SIGNAL, wsId);
    }

    @Override
    public void wsUnreachable(Identifier<WebServer> wsId) {
        signal(UNREACH_WS_SIGNAL, wsId);
    }

    @Override
    public CurrentGroupState signalReset(User user) {
        signalFromUser(RESET_REQUESTED,STATES.get(currentState).resetState, user);
        return getCurrentStateDetail();
    }

    @Override
    public void jvmStarted(Identifier<Jvm> jvmId) {
        signal(START_JVM_SIGNAL, jvmId);
    }

    @Override
    public void jvmStopped(Identifier<Jvm> jvmId) {
        signal(STOP_JVM_SIGNAL, jvmId);
    }

    @Override
    public void jvmError(Identifier<Jvm> jvmId) {
        signal(ERROR_JVM_SIGNAL, jvmId);
    }

    @Override
    public CurrentGroupState signalStartRequested(User user) {
        signalFromUser(START_REQUESTED,GroupState.STARTING, user);
        return getCurrentStateDetail();
    }

    @Override
    public CurrentGroupState signalStopRequested(User user) {
        signalFromUser(STOP_REQUESTED,GroupState.STOPPING, user);
        return getCurrentStateDetail();
    }


    @Override
    public boolean refreshState() {
        if(stateLock.tryAcquire()) {
            try {
                synchronizedHandleState(currentState, SYSTEM_USER);
            } finally { 
                stateLock.release();
            }
            return true;
        }
        return false;
    }

    // ============= query methods ==============

    @Override
    public boolean canStart() {
        return STATES.get(currentState).canStart == CAN_START;
    }

    @Override
    public boolean canStop() {
        return STATES.get(currentState).canStop == CAN_STOP;
    }

    @Override
    public CurrentGroupState getCurrentStateDetail() {
        return new CurrentGroupState(getEntity().getId(), currentState, lastChange, jvmsDetail, webServersDetail);
    }

    @Override
    public Group getCurrentGroup() {
        return getEntity();
    }

}
