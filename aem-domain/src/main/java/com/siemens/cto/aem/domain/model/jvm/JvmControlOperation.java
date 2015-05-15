package com.siemens.cto.aem.domain.model.jvm;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;

public enum JvmControlOperation {

    START("start", JvmState.JVM_START, new String[] {"The requested service has already been started."}),
    STOP("stop", JvmState.JVM_STOP, new String[] {/*net*/"The (.*) service is not started.",
                                                  /*sc*/"The service has not been started.",
                                                  /*script*/"The service has not been started."}),
    THREAD_DUMP("threadDump", null, null),
    HEAP_DUMP("heapDump", null, null);

    private static final Map<String, JvmControlOperation> LOOKUP_MAP = new HashMap<>();

    static {
        for (final JvmControlOperation operation : values()) {
            LOOKUP_MAP.put(operation.operationValue.toLowerCase(), operation);
        }
    }

    private final String operationValue;
    private final JvmState operationState;
    private final Pattern[] successOutputPattern;

    private JvmControlOperation(final String theValue,
                                final JvmState theOperationJvmState,
                                final String[] successOutputRegex) {
        operationValue = theValue;
        operationState = theOperationJvmState;
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

    /** Compare against success strings */
    public boolean checkForSuccess(String output) { 
        for(Pattern pattern : successOutputPattern) {
            if(pattern.matcher(output).find()) return true;
        }
        return false;
    }
}
