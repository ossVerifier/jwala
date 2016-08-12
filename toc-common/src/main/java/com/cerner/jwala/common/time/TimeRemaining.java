package com.cerner.jwala.common.time;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TimeRemaining {

    private final TimeDuration timeRemaining;
    private final boolean isTimeRemaining;

    public TimeRemaining(final TimeDuration theTimeRemaining) {
        timeRemaining = theTimeRemaining;
        isTimeRemaining = (theTimeRemaining.valueOf() > 0);
    }

    public TimeDuration getDuration() {
        return timeRemaining;
    }

    public boolean isTimeRemaining() {
        return isTimeRemaining;
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
        TimeRemaining rhs = (TimeRemaining) obj;
        return new EqualsBuilder()
                .append(this.timeRemaining, rhs.timeRemaining)
                .append(this.isTimeRemaining, rhs.isTimeRemaining)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(timeRemaining)
                .append(isTimeRemaining)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timeRemaining", timeRemaining)
                .append("isTimeRemaining", isTimeRemaining)
                .toString();
    }
}
