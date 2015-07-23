package com.siemens.cto.aem.domain.model.audit;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class AuditDateTime implements Serializable {

    public static AuditDateTime now() {
        return new AuditDateTime(new Date());
    }

    private static final long serialVersionUID = 1L;

    private final Date date;

    public AuditDateTime(final Date theDateTime) {
        this.date = theDateTime;
    }

    public Date getDate() {
        return date;
    }

    public Calendar getCalendar() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
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
        AuditDateTime rhs = (AuditDateTime) obj;
        return new EqualsBuilder()
                .append(this.date, rhs.date)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(date)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("date", date)
                .toString();
    }
}
