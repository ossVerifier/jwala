package com.siemens.cto.aem.service.group.impl;

//import java.util.HashMap;
import java.util.Map;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.temporary.User;

/**
 * FSM built using spEL for handlers (Spring Expression Language)
 * TODO Ken had the idea of actually storing the state transition triggers in the states, so it is a true state transition table. 
 * TODO Remove the conditions.
 * TODO Add Interface
 */
public class JvmStateManagerTableImpl {}
//extends AbstractStateManager<JvmState, Jvm, JvmStateManagerTableImpl.StartCondition, JvmStateManagerTableImpl.StopCondition> 
// implements JvmStateMachine
//{
//
//    // Conditions - properties of being in a state
//    enum StartCondition { 
//        CAN_START,
//        CANNOT_START
//    }
//    
//    static enum StopCondition { 
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
//
//    @SuppressWarnings("unused")
//    private static final User   SYSTEM_USER;
//    private static final Map<ExternalizableState, StateEntry<JvmState, Jvm, JvmStateManagerTableImpl.StartCondition, JvmStateManagerTableImpl.StopCondition>> STATES;
//    
//    private static class JvmStateEntry extends StateEntry<JvmState, Jvm, JvmStateManagerTableImpl.StartCondition, JvmStateManagerTableImpl.StopCondition> {
//
//        JvmStateEntry(ExpressionParser parser, StartCondition canStart, StopCondition canStop, JvmState resetState,
//                String in, String stateHandler, String out) {
//            super(parser, canStart, canStop, resetState, in, stateHandler, out);
//        }
//        
//    }
//    // =========== STATE TRANSITION TABLE =============================
//    static { 
//        SYSTEM_USER = User.getSystemUser();
//
//        STATES = new HashMap<>();
//        ExpressionParser parser = new SpelExpressionParser();
//
//        STATES.put(null,                    new JvmStateEntry(parser, StartCondition.CANNOT_START,StopCondition.CANNOT_STOP, null,        NO_OP,      NO_OP,      NO_OP));
//        STATES.put(JvmState.UNKNOWN,        new JvmStateEntry(parser, StartCondition.CANNOT_START,StopCondition.CANNOT_STOP, null,        NO_OP,      NO_OP,      NO_OP));
//        STATES.put(JvmState.FAILED,         new JvmStateEntry(parser, StartCondition.CANNOT_START,StopCondition.CANNOT_STOP, JvmState.INITIALIZED, null,      null,       null));
//        STATES.put(JvmState.INITIALIZED,    new JvmStateEntry(parser, StartCondition.CAN_START,   StopCondition.CAN_STOP,    JvmState.INITIALIZED, null,      null,       null));
//        STATES.put(JvmState.START_REQUESTED,new JvmStateEntry(parser, StartCondition.CANNOT_START,StopCondition.CANNOT_STOP, JvmState.INITIALIZED, null,      null,       null));
//        STATES.put(JvmState.STARTED,        new JvmStateEntry(parser, StartCondition.CANNOT_START,StopCondition.CAN_STOP,    JvmState.INITIALIZED, null,      null,       null));
//        STATES.put(JvmState.STOP_REQUESTED, new JvmStateEntry(parser, StartCondition.CANNOT_START,StopCondition.CANNOT_STOP, JvmState.INITIALIZED, null,      null,       null));
//        STATES.put(JvmState.STOPPED,        new JvmStateEntry(parser, StartCondition.CAN_START,   StopCondition.CANNOT_STOP, JvmState.INITIALIZED, null,      null,       null));
//
//    }
//   
//    public JvmStateManagerTableImpl() {
//        super(STATES, JvmState.INITIALIZED);
//    }
//    // ================== Beans used (currently none) ======================
//    
////    @Autowired
////    JvmPersistenceService jvmPersistenceService;
//    
//    // ================== state methods below ==============================
//    
//    // public JvmState onRequestToStart() { }
//    
//    // ================== trigger methods below (goes in interface) ========
//    
//    public boolean requestToStart(User user) { 
//        handleState(JvmState.START_REQUESTED, user);
//        return currentState == JvmState.START_REQUESTED;
//    }
//    public void requestToStop(User user) { 
//        handleState(JvmState.STOP_REQUESTED, user);
//    }
//}
