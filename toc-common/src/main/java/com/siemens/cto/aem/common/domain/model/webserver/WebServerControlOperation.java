package com.siemens.cto.aem.common.domain.model.webserver;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

public enum WebServerControlOperation {

    START("start", WebServerReachableState.WS_START_SENT),
    STOP("stop", WebServerReachableState.WS_STOP_SENT),
    VIEW_HTTP_CONFIG_FILE("viewHttpConfigFile", null),
    DEPLOY_HTTP_CONFIG_FILE("deployHttpConfigFile", null),
    BACK_UP_HTTP_CONFIG_FILE("backUpHttpConfigFile", null);

    private static final Map<String, WebServerControlOperation> LOOKUP_MAP = new HashMap<>();

    static {
        for (final WebServerControlOperation operation : values()) {
            LOOKUP_MAP.put(operation.operationValue.toLowerCase(), operation);
        }
    }

    private final String operationValue;
    private final WebServerReachableState operationState;

    private WebServerControlOperation(final String theValue,
                                      final WebServerReachableState theOperationState) {
        operationValue = theValue;
        operationState = theOperationState;
    }

    public static WebServerControlOperation convertFrom(final String aValue) throws BadRequestException {
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

    public WebServerReachableState getOperationState() {
        return operationState;
    }
}
