package com.siemens.cto.aem.common.domain.model.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;

import java.util.HashMap;
import java.util.Map;

public enum GroupControlOperation {

    START("start"),
    STOP("stop");

    private static final Map<String, GroupControlOperation> LOOKUP_MAP = new HashMap<>();

    static {
        for (final GroupControlOperation operation : values()) {
            LOOKUP_MAP.put(operation.operationValue.toLowerCase(), operation);
        }
    }

    private final String operationValue;

    private GroupControlOperation(final String theValue) {
        operationValue = theValue;
    }

    public static GroupControlOperation convertFrom(final String aValue) throws BadRequestException {
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
}
