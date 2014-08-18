package com.siemens.cto.aem.service.group.impl;

import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.temporary.User;

/**
 * FSM built using spEL for handlers (Spring Expression Language)
 */
public class AbstractStateManager<StatesEnum extends Enum<?>, Entity, Condition1, Condition2> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractStateManager.class);

    // Define what 'null' means for StateEntries
    public static final String DEFAULT_STATE_IN_TRANSITION_EXPRESSION = "#enteringState";
    public static final String DEFAULT_STATE_OUT_TRANSITION_EXPRESSION = "defaultStateOutTransitionHandler(#exitingState)";
    public static final String DEFAULT_STATE_EXPRESSION = "defaultStateHandler(#state)";
    public static final String NO_OP="currentState";

    // Variables For reporting state detail
    protected DateTime                        lastChange;

    // Tracking state changes internally during a state transition
    protected Entity              currentEntity;
    protected StatesEnum          currentState;
    protected Identifier<Entity>  currentId;
    
    // Key Enum instances
    StatesEnum                  initializedState;

    // Internal implementation
    private EvaluationContext   context;
    private Map<ExternalizableState, StateEntry<StatesEnum, Entity, Condition1, Condition2>> STATES;
       
    // Define what 'null' means for a return value from a state transition handler
    public final StatesEnum CONTINUE = null;
    
    // =========== CONSTRUCTOR ===================

    public AbstractStateManager(final Map<ExternalizableState, StateEntry<StatesEnum, Entity, Condition1, Condition2>> states, final StatesEnum initializedState) {
        context = new StandardEvaluationContext(this);
        
        // this accessor lets you access states by name in spEL!
        context.getPropertyAccessors().add(new PropertyAccessor() {
            
            @Override
            public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
                // never called, because canWrite() { return false; }
            }
            
            @Override
            public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
                @SuppressWarnings("unchecked")
                StatesEnum e = (StatesEnum) Enum.valueOf(initializedState.getClass(), name);
                if(e == null) { 
                    return null;
                } else { 
                   return new TypedValue(e);
                }
            }
            
            @Override
            public Class<?>[] getSpecificTargetClasses() {                
                return new Class[] { AbstractStateManager.this.getClass() };
            }
            
            @Override
            public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {                
                return false;
            }
            
            @Override
            public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
                try { 
                    StatesEnum e = (StatesEnum) Enum.valueOf(initializedState.getClass(), name);
                    return e != null;
                } catch (IllegalArgumentException e) { 
                    return false; // invalid enum value
                }                
            }
        });
        this.initializedState = initializedState;
        STATES = states;
    }

    // =========== STATE ENGINE =============================

    /**
     * Changes state. After a state is entered, it is 'IN'
     * @param proposedState
     */
    protected synchronized void synchronizedHandleState(StatesEnum proposedState, User user) {
        StatesEnum nextState;

        try {

            if(proposedState == null) {
                // do nothing
                return;
            }

            nextState = proposedState;

            if(proposedState == currentState) {
                // staying in same state
                nextState = (StatesEnum) STATES.get(currentState).state(context, this, currentState);
                if(nextState == currentState) {
                    return; // no change.
                }
            }

            // Temporary variables
            if(nextState == null) {
                nextState = proposedState;
            }
            StatesEnum interimState = currentState;
            StatesEnum nextState2 = nextState;

            // while state changes required.
            while(nextState != interimState) {
                // Exit current state.
                nextState2 = STATES.get(interimState).out(context,this, interimState);
                // New state change proposed?
                if(nextState2 != null) {
                    nextState = nextState2;
                }

                // If we have somewhere to go ( on the first transition we do ),
                if(nextState != null) {
                    // Enter new state, record proposition
                    nextState2 = STATES.get(nextState).in(context, this, nextState);
                    // track current 'interim' state
                    interimState = nextState;
                    // new state change proposed ?
                    if(nextState2 != null && nextState2 != nextState) {
                        // Otherwise, yet another state transition?
                        nextState = nextState2;
                    }
                    // else null or nextState = ok, so enter.
                }
            }
            currentState = interimState;
        } finally {
            // this.triggers.drain();
        }
    }

    // ========= INITIALIZER ===============
    
    public synchronized void initialize(Entity entity, Identifier<Entity> id, StatesEnum state, DateTime asof, User user) {
        currentState = null;
        lastChange = asof;
        currentEntity = entity;
        currentId = id;

        // invoke FSM for the first time. Should change currentState.
        synchronizedHandleState(state == null ? initializedState : state, user);
    }

    public StatesEnum getCurrentState() {
        return currentState;
    }

    public Entity getEntity() {
        return currentEntity;
    }
    
    public Identifier<Entity> getEntityId() {
        return currentId;
    }

    // ========== STATE MAP ENTRY HOLDER CLASS  ================

    static class StateEntry<StatesEnum extends Enum<?>, Entity, Condition1, Condition2> {
        private static final String SPELVAR_CURRENT_STATE = "state";
        private static final String SPELVAR_EXITING_STATE = "exitingState";
        private static final String SPELVAR_ENTERING_STATE = "enteringState";
        public Condition1 canStart;
        public Condition2 canStop;
        public  StatesEnum   resetState;
        private Expression   stateInExpression;
        private Expression   stateExpression;
        private Expression   stateOutExpression;
        StateEntry(Condition1 canStart, Condition2 canStop, StatesEnum resetState) {
            this.canStart = canStart;
            this.canStop = canStop;
            this.resetState = resetState;
        }
        StateEntry(ExpressionParser parser, Condition1 canStart, Condition2 canStop, StatesEnum resetState, String in, String stateHandler, String out) {
            this(canStart, canStop, resetState);
            this.stateInExpression = parser.parseExpression(in != null ? in : DEFAULT_STATE_IN_TRANSITION_EXPRESSION);
            this.stateOutExpression = parser.parseExpression(out != null ? out : DEFAULT_STATE_OUT_TRANSITION_EXPRESSION);
            this.stateExpression = parser.parseExpression(stateHandler != null ? stateHandler : DEFAULT_STATE_EXPRESSION);
        }

        @SuppressWarnings("unchecked")
        StatesEnum in(EvaluationContext context, AbstractStateManager<StatesEnum, Entity, Condition1, Condition2> fsm, StatesEnum enteringState) {
            try {
                fsm.debug("ENTER", enteringState);
                context.setVariable(SPELVAR_ENTERING_STATE, enteringState);
                return (StatesEnum)this.stateInExpression.getValue(context, fsm);
            } finally {
                context.setVariable(SPELVAR_ENTERING_STATE, null);
            }
        }
        @SuppressWarnings("unchecked")
        StatesEnum out(EvaluationContext context, AbstractStateManager<StatesEnum, Entity, Condition1, Condition2> fsm, StatesEnum exitingState) {
            try {
                fsm.debug("EXIT", exitingState);
                context.setVariable(SPELVAR_EXITING_STATE, exitingState);
                return (StatesEnum) this.stateOutExpression.getValue(context, fsm);
            } finally {
                context.setVariable(SPELVAR_EXITING_STATE, null);
            }
        }
        @SuppressWarnings("unchecked")
        StatesEnum state(EvaluationContext context, AbstractStateManager<StatesEnum, Entity, Condition1, Condition2> fsm, StatesEnum state) {
            try {
                fsm.debug("VERIFY", state);
                context.setVariable(SPELVAR_CURRENT_STATE, state);
                return (StatesEnum) this.stateExpression.getValue(context, fsm);
            } finally {
                context.setVariable(SPELVAR_CURRENT_STATE, null);
            }
        }
    }

    // ==== Log Helper - override as you please ====

    protected void debug(String op, StatesEnum state) {
        if(state != null) {
            LOGGER.debug("FSM for id={}: {} {}", getEntityId().getId(), op, state);
        }
    }
    
    // =================== Default State Handlers ==================
    
    /**
     * This is the DEFAULT_STATE_OUT_TRANSITION_HANDLER
     * @return
     */
    public StatesEnum defaultStateOutTransitionHandler(StatesEnum exitingState) {
        return CONTINUE;
    }

    /**
     * This is the DEFAULT_STATE_HANDLER
     * @return
     */
    public StatesEnum defaultStateHandler(StatesEnum state) {
        return CONTINUE;
    }
}
