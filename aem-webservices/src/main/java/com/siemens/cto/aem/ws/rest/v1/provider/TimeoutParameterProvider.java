package com.siemens.cto.aem.ws.rest.v1.provider;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.QueryParam;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.time.TimeDuration;
import com.siemens.cto.aem.ws.rest.v1.fault.RestFaultType;

public class TimeoutParameterProvider {

    private static final TimeDuration DEFAULT_TIMEOUT_VALUE = new TimeDuration(30L, TimeUnit.SECONDS);

    @QueryParam("timeout")
    private String timeout;

    public TimeoutParameterProvider() {
    }

    public TimeoutParameterProvider(final String aTimeout) {
        timeout = aTimeout;
    }

    public TimeDuration valueOf() throws BadRequestException {
        if (isParameterPresent()) {
            return parseValue();
        }

        return DEFAULT_TIMEOUT_VALUE;
    }

    TimeDuration parseValue() throws BadRequestException {
        try {
            final Long timeoutValue = Long.valueOf(timeout);
            validate(timeoutValue);
            return new TimeDuration(timeoutValue,
                                    TimeUnit.SECONDS);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(RestFaultType.INVALID_TIMEOUT_PARAMETER,
                                          "Non-integer value specified for the timeout parameter");
        }
    }

    boolean isParameterPresent() {
        return (timeout != null);
    }

    void validate(final Long aValue) throws BadRequestException {
        if ((aValue <= 0) || (aValue > 60)) {
            throw new BadRequestException(RestFaultType.INVALID_TIMEOUT_PARAMETER,
                                          "Timeout value was outside the allowable range : " + aValue);
        }
    }
}
