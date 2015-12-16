package com.siemens.cto.aem.common.domain.model.app;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

public enum ApplicationControlOperation {

    DEPLOY_WAR("deployWar"),
    DEPLOY_CONFIG_FILE("deployConfigFile"),
    BACK_UP_CONFIG_FILE("backUpHttpConfigFile");

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

    public static ApplicationControlOperation convertFrom(final String aValue) throws BadRequestException {
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
