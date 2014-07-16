package com.siemens.cto.aem.service.group.impl;

import static com.siemens.cto.aem.domain.model.group.GroupState.ERROR;
import static com.siemens.cto.aem.domain.model.group.GroupState.INITIALIZED;
import static com.siemens.cto.aem.domain.model.group.GroupState.PARTIAL;
import static com.siemens.cto.aem.domain.model.group.GroupState.STARTED;
import static com.siemens.cto.aem.domain.model.group.GroupState.STARTING;
import static com.siemens.cto.aem.domain.model.group.GroupState.STOPPED;
import static com.siemens.cto.aem.domain.model.group.GroupState.STOPPING;
import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StartCondition.CANNOT_START;
import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StartCondition.CAN_START;
import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StopCondition.CANNOT_STOP;
import static com.siemens.cto.aem.service.group.impl.GroupStateManagerTableImpl.StopCondition.CAN_STOP;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;

// Supports reflection or Spring Expression Language
public class GroupStateManagerTableImpl {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupStateManagerTableImpl.class);

    // Define what 'null' means for StateEntries
    public static final String DEFAULT_STATE_IN_TRANSITION_HANDLER = "defaultStateInTransitionHandler";
    public static final String DEFAULT_STATE_OUT_TRANSITION_HANDLER = "defaultStateOutTransitionHandler";
    public static final String DEFAULT_STATE_HANDLER = "defaultStateHandler";
    public static final String DEFAULT_STATE_IN_TRANSITION_EXPRESSION = "defaultStateInTransitionHandler()";
    public static final String DEFAULT_STATE_OUT_TRANSITION_EXPRESSION = "defaultStateOutTransitionHandler()";
    public static final String DEFAULT_STATE_EXPRESSION = "defaultStateHandler()";
    public static final String NO_OP="currentState";

    private EvaluationContext context;
    private Group       currentGroup;
    private GroupState  currentState;
    private Triggers triggers = new Triggers();

    private static final Map<GroupState, StateEntry> gse;

    static { 
        gse = new HashMap<>();
        ExpressionParser parser = new SpelExpressionParser();
        
        gse.put(null,           new StateEntry(parser, CANNOT_START,CANNOT_STOP,null,       NO_OP,      NO_OP,      NO_OP));
        gse.put(ERROR,          new StateEntry(parser, CANNOT_START,CANNOT_STOP,INITIALIZED, null,      null,       null));
        gse.put(INITIALIZED,    new StateEntry(parser, CAN_START,   CAN_STOP,   null,       "onInitializeIn()", null, null));
        gse.put(PARTIAL,        new StateEntry(parser, CAN_START,   CAN_STOP,   null,       null, "onPartial()",    null));
        gse.put(STARTING,       new StateEntry(parser, CANNOT_START,CANNOT_STOP, null,      null, "onStarting()",   null));
        gse.put(STARTED,        new StateEntry(parser, CANNOT_START,CAN_STOP,   null,       null, "onStarted()",    null));
        gse.put(STOPPING,       new StateEntry(parser, CANNOT_START,CANNOT_STOP, null,      null, "onStopping()",   null));
        gse.put(STOPPED,        new StateEntry(parser, CAN_START,   CANNOT_STOP, null,      null, "onStopped()",    null));

        // reflection based calls rather than spEL: can be removed if all goes well.
        /*
        gse.put(ERROR, new StateEntry(CANNOT_START,CANNOT_STOP,INITIALIZED, null, null, null));
        gse.put(INITIALIZED, new StateEntry(CAN_START,CAN_STOP, null, method("onInitializeIn"), null, null));
        gse.put(PARTIAL, new StateEntry(CAN_START,CAN_STOP, null, null, method("onPartial"), null));
        gse.put(STARTING, new StateEntry(CANNOT_START,CANNOT_STOP, null, null, method("onStarting"), null));
        gse.put(STARTED, new StateEntry(CANNOT_START,CAN_STOP, null, null, method("onStarted"), null));
        gse.put(STOPPING, new StateEntry(CANNOT_START,CANNOT_STOP, null, null, method("onStopping"), null));
        gse.put(STOPPED, new StateEntry(CAN_START,CANNOT_STOP, null, null, method("onStopped"), null));
         */
        
    }
    
    private class Triggers { 
        public ConcurrentLinkedDeque<Identifier<Jvm>> jvms = new ConcurrentLinkedDeque<>();
    }
    
    enum StartCondition { 
        CAN_START,
        CANNOT_START
    }
    enum StopCondition { 
        CAN_STOP,
        CANNOT_STOP
    }
    
    // =========== HANDLER INJECTED BEANS ===================
    @Autowired 
    GroupPersistenceService groupPersistenceService;
    
    @Autowired
    JvmStatePersistenceService jvmStatePersistenceService;
    
    // =========== STATE HANDLERS ===========================
    // Note: all state handler methods must be public
    // But should not be invoked directly.
    
    /**
     * This is the DEFAULT_STATE_IN_TRANSITION_HANDLER
     * @return
     */
    public GroupState defaultStateInTransitionHandler() { 
        // use this to persist the transition to the Group table.
        return null;
    }

    /**
     * This is the DEFAULT_STATE_OUT_TRANSITION_HANDLER
     * @return
     */
    public GroupState defaultStateOutTransitionHandler() {
        return null;
    }

    /**
     * This is the DEFAULT_STATE_HANDLER
     * @return
     */
    public GroupState defaultStateHandler() { 
        return null;
    }
    
    /**
     * Responsible for going from initialized to a real state
     * @return
     */
    public GroupState onInitializeIn() {        
        // check with injected persistence service for number of active jvms to decide which state this 
        // should really be in.
        // then call         defaultStateInTransitionHandler(); or persist.
        return getPerceivedState();
    }

    /**
     * Starting state, immediate
     * @return
     */
    public GroupState onStarting() {
        return GroupState.STARTED;
    }


    /**
     * Stopping state, immediate
     * @return
     */
    public GroupState onStopping() {
        return GroupState.STOPPED;
    }


    /**
     * Stay Started
     * @return
     */
    public GroupState onStarted() {
        return getPerceivedState();
    }

    /**
     * Stay Stopped
     * @return
     */
    public GroupState onStopped() {
        return getPerceivedState();
    }

    /**
     * Stay Partial
     * @return
     */
    public GroupState onPartial() {
        return getPerceivedState();
    }

    // =========== STATE HELPERS ============================
    
    private GroupState getPerceivedState() { 
        
        Group group = groupPersistenceService.getGroup(getCurrentGroup().getId());
        int started = 0, unstarted = 0;
        for(Jvm jvm : group.getJvms()) {
            CurrentJvmState jvmState = jvmStatePersistenceService.getJvmState(jvm.getId());
            if(jvmState.getJvmState() == JvmState.STARTED) { 
                ++started;
            } else { 
                ++unstarted;
            }
        }
        if(started == 0 && unstarted == 0) { 
            return GroupState.INITIALIZED;
        } else if(started == 0) {
            return GroupState.STOPPED;
        } else if(started > 0){ 
            return GroupState.PARTIAL;
        } else { 
            return GroupState.STARTED;
        }
    }
    
    // =========== STATE ENGINE =============================
    
    /**
     * Changes state. After a state is entered, it is 'IN'
     * @param proposedState
     */
    private synchronized void handleState(GroupState proposedState) {
        if(proposedState == null) { 
            // do nothing
            return; 
        }

        GroupState nextState = proposedState;

        if(proposedState == currentState) {
            // staying in same state
            nextState = (GroupState) gse.get(currentState).state(context, this);
            if(nextState == currentState) { 
                return; // no change.
            }
        } 

        // Temporary variables
        if(nextState == null) {
            nextState = proposedState;
        }
        GroupState interimState = currentState;
        GroupState nextState2 = nextState;

        // while state changes required.  
        while(nextState != interimState) {
            // Exit current state.
            nextState2 = gse.get(interimState).out(context,this);
            // New state change proposed?
            if(nextState2 != null) {
                nextState = nextState2;
            }
            
            // If we have somewhere to go ( on the first transition we do ), 
            if(nextState != null) {
                // Enter new state, record proposition
                nextState2 = gse.get(nextState).in(context, this);
                // track current 'interim' state
                interimState = nextState;
                // new state change proposed ?
                if(nextState2 != null && nextState2 != nextState) { 
                    // Otherwise, yet another state transition?
                    nextState = nextState2;
                } // else null or nextState = ok, enter. 
            }
        }        
        currentState = interimState;
    }
    
    // ========== API to this state machine ================

    public GroupStateManagerTableImpl() {
        context = new StandardEvaluationContext(this);
    }
    
    public void initializeGroup(Group group) { 
        currentState = null;
        currentGroup = group;
        
        // invoke FSM for the first time. Should change currentState. 
        handleState(group.getState());
    }
    
    public void signalReset() { 
        handleState(gse.get(currentState).resetState);
    }
    
    public boolean canStart() {
        return gse.get(currentState).canStart == CAN_START;
    }
    
    public boolean canStop() {
        return gse.get(currentState).canStop == CAN_STOP;
    }
    
    public void jvmStarted(Identifier<Jvm> jvmId) {        
        triggers.jvms.add(jvmId);
        handleState(currentState);
    }

    public GroupState getCurrentState() {
        return currentState;
    }

    public Group getCurrentGroup() {
        return currentGroup;
    }

    // ========== STATE MAP ENTRY HOLDER CLASS  ================
    

    private static class StateEntry {
        public StartCondition canStart;
        public StopCondition canStop;
        public GroupState   resetState;
        public Method       stateInHandler;
        public Method       stateHandler;        
        public Method       stateOutHandler;
        public Expression   stateInExpression;
        public Expression   stateExpression;        
        public Expression   stateOutExpression;
        StateEntry(StartCondition canStart, StopCondition canStop, GroupState resetState) {
            this.canStart = canStart;
            this.canStop = canStop;
            this.resetState = resetState;
        }
        StateEntry(StartCondition canStart, StopCondition canStop, GroupState resetState, Method in, Method stateHandler, Method out) {
            this(canStart, canStop, resetState);
            this.stateInHandler = in != null ? in : method(DEFAULT_STATE_IN_TRANSITION_HANDLER);
            this.stateOutHandler = out != null ? out : method(DEFAULT_STATE_OUT_TRANSITION_HANDLER);
            this.stateHandler = stateHandler != null ? stateHandler: method(DEFAULT_STATE_HANDLER);
        }
        StateEntry(ExpressionParser parser, StartCondition canStart, StopCondition canStop, GroupState resetState, String in, String stateHandler, String out) {
            this(canStart, canStop, resetState);
            this.stateInExpression = parser.parseExpression(in != null ? in : DEFAULT_STATE_IN_TRANSITION_EXPRESSION);
            this.stateOutExpression = parser.parseExpression(out != null ? in : DEFAULT_STATE_OUT_TRANSITION_EXPRESSION);
            this.stateExpression = parser.parseExpression(stateHandler != null ? in : DEFAULT_STATE_EXPRESSION);
        }
        
        GroupState in(EvaluationContext context, GroupStateManagerTableImpl fsm) { 
            if(stateInExpression != null) {
                return this.stateInExpression.getValue(context, fsm, GroupState.class);
            } else if(stateInHandler != null) {
                return invoke(this.stateInHandler,fsm);
            } else {
                return fsm.currentState;
            }
        }
        GroupState invoke(Method m, GroupStateManagerTableImpl fsm) {
            try {
                GroupState result = (GroupState) m.invoke(fsm);
                return result;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.error("Failed to transition Group states for Group " + fsm.currentGroup, e);
                return ERROR;
            }
        }
        GroupState out(EvaluationContext context, GroupStateManagerTableImpl fsm) { 
            if(stateOutExpression != null) {
                return this.stateOutExpression.getValue(context, fsm, GroupState.class);
            } else if(stateOutHandler != null) {
                return invoke(this.stateOutHandler,fsm);
            } else {
                return fsm.currentState;
            }
        }
        GroupState state(EvaluationContext context, GroupStateManagerTableImpl fsm) { 
            if(stateExpression != null) {
                return this.stateExpression.getValue(context, fsm, GroupState.class);
            } else if(stateHandler != null) {
                return invoke(this.stateHandler,fsm);
            } else {
                return fsm.currentState;
            }            
        }

    }
    
    // ========== CONFIG HELPERS ================
    
    /**
     * Get a Method through reflection in order to invoke state handlers.
     * @param stateHandlerMethodName method on this class.
     * @return
     */
    private static Method method(String stateHandlerMethodName) { 
        try {
            return GroupStateManagerTableImpl.class.getMethod(stateHandlerMethodName);
        } catch (NoSuchMethodException | SecurityException e) {
            LOGGER.error("Programming error: failed to set up group state machine.");
        }
        return null;
    }

}
