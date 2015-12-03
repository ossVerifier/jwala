package com.siemens.cto.aem.common.time;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class TimeDuration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long timePeriod;
    private final TimeUnit timeUnit;

    public TimeDuration(final Long theTimePeriod,
                        final TimeUnit theTimeUnit) {
        timePeriod = theTimePeriod;
        timeUnit = theTimeUnit;
    }

    public TimeDuration convertTo(final TimeUnit aNewUnit) {
        return new TimeDuration(aNewUnit.convert(timePeriod,
                                                 timeUnit),
                                aNewUnit);
    }

    public Long valueOf(final TimeUnit aNewUnit) {
        return convertTo(aNewUnit).valueOf();
    }

    public Long valueOf() {
        return timePeriod;
    }

    public TimeUnit getUnit() {
        return timeUnit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        TimeDuration rhs = (TimeDuration) obj;
        return new EqualsBuilder()
                .append(this.timePeriod, rhs.timePeriod)
                .append(this.timeUnit, rhs.timeUnit)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(timePeriod)
                .append(timeUnit)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timePeriod", timePeriod)
                .append("timeUnit", timeUnit)
                .toString();
    }
}
