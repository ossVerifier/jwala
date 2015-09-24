package com.siemens.cto.aem.domain.model.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.state.CurrentState;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.siemens.cto.aem.domain.AemDomain.*;
import static com.siemens.cto.aem.domain.model.jvm.JvmState.*;

/**
 * Enumeration of control operations that can be executed against a JVM
 */
public enum JvmControlOperation {

    START(  "start",JVM_START, NO_JVM_COMPLETE_STATE, JVM_FAILED,
                    new String[] {
                            /*net*/"The requested service has already been started.",
                            /*sc*/"An instance of the service is already running."                            
    }
                    ),
                    
    STOP(   "stop", JVM_STOP, SVC_STOPPED, JVM_FAILED,
                    new String[] {/*net*/"The (.*) service is not started.",
                                  /*sc*/"The service has not been started.",
                                  /*script*/"The service has not been started."}),
                                  
    THREAD_DUMP(
            "threadDump",   NO_JVM_IN_PROGRESS_STATE, NO_JVM_COMPLETE_STATE, NO_JVM_FAILURE_STATE, 
                            NO_JVM_SUCCESS_KEYWORDS),
            
    HEAP_DUMP("heapDump",   NO_JVM_IN_PROGRESS_STATE, NO_JVM_COMPLETE_STATE, NO_JVM_FAILURE_STATE, 
                            NO_JVM_SUCCESS_KEYWORDS),

    DEPLOY_CONFIG_TAR("deployConfigTar",   NO_JVM_IN_PROGRESS_STATE, NO_JVM_COMPLETE_STATE, NO_JVM_FAILURE_STATE,
                            NO_JVM_SUCCESS_KEYWORDS),

    DELETE_SERVICE("deleteService", NO_JVM_IN_PROGRESS_STATE, NO_JVM_COMPLETE_STATE, NO_JVM_FAILURE_STATE, NO_JVM_SUCCESS_KEYWORDS),

    INVOKE_SERVICE("invokeService", NO_JVM_IN_PROGRESS_STATE, NO_JVM_COMPLETE_STATE, NO_JVM_FAILURE_STATE, NO_JVM_SUCCESS_KEYWORDS);

    private static final Map<String, JvmControlOperation> LOOKUP_MAP = new HashMap<>();

    static {
        for (final JvmControlOperation operation : values()) {
            LOOKUP_MAP.put(operation.operationValue.toLowerCase(), operation);
        }
    }

    private final String operationValue;
    private final JvmState operationState;
    private final JvmState confirmedState;
    private final JvmState failureState;
    private final Pattern[] successOutputPattern;

    private JvmControlOperation(final String theValue,
                                final JvmState theOperationJvmState,
                                final JvmState theConfirmedState,
                                final JvmState theFailureState,
                                final String[] successOutputRegex) {
        operationValue = theValue;
        operationState = theOperationJvmState;
        confirmedState = theConfirmedState;
        failureState   = theFailureState;
        
        if(successOutputRegex != null) {
            successOutputPattern = new Pattern[successOutputRegex.length];
            int i = 0;
            for(String regex : successOutputRegex) {
                successOutputPattern[i++] = Pattern.compile(regex);
            }
        } else {
            successOutputPattern = new Pattern[0];
        }
    }

    public static JvmControlOperation convertFrom(final String aValue) throws BadRequestException {
        final String value = aValue.toLowerCase();
        if (LOOKUP_MAP.containsKey(value)) {
            return LOOKUP_MAP.get(value);
        }

        throw new BadRequestException(AemFaultType.INVALID_JVM_OPERATION,
                                      "Invalid operation: " + aValue);
    }

    public String getExternalValue() {
        return operationValue;
    }

    public JvmState getOperationState() {
        return operationState;
    }

    public JvmState getConfirmedState() {
        return confirmedState;
    }

    public JvmState getFailureStateOrPrevious(CurrentState<Jvm, JvmState> prevState) {
        return prevState == null ? failureState : (failureState == null ? prevState.getState() : failureState);
    }

    public JvmState getFailureState() {
        return failureState;
    }

    /** Compare against success strings */
    public boolean checkForSuccess(String output) { 
        for(Pattern pattern : successOutputPattern) {
            if(pattern.matcher(output).find()) return true;
        }
        return false;
    }
}
