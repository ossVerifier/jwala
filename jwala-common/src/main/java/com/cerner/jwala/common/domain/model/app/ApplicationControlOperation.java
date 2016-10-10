package com.cerner.jwala.common.domain.model.app;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

public enum ApplicationControlOperation {

    SECURE_COPY("scp"),
    BACK_UP("backUpFile"),
    CREATE_DIRECTORY("mkdir"),
    CHANGE_FILE_MODE("chmod"),
    CHECK_FILE_EXISTS("test");

    private static final Map<String, ApplicationControlOperation> LOOKUP_MAP = new HashMap<>();

    static {
        for (final ApplicationControlOperation operation : values()) {
            LOOKUP_MAP.put(operation.operationValue.toLowerCase(), operation);
        }
    }

    private final String operationValue;

    private ApplicationControlOperation(final String theValue) {
        operationValue = theValue;
    }

    public static ApplicationControlOperation convertFrom(final String aValue) {
        final String value = aValue.toLowerCase();
        if (LOOKUP_MAP.containsKey(value)) {
            return LOOKUP_MAP.get(value);
        }

        throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_OPERATION,
                "Invalid operation: " + aValue);
    }

    public String getExternalValue() {
        return operationValue;
    }

}
