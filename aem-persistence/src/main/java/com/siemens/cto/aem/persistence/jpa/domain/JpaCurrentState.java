package com.siemens.cto.aem.persistence.jpa.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "current_state")
public class JpaCurrentState implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private JpaCurrentStateId id;

    @Column(name = "STATE",
            nullable = false)
    private String state;

    @Column(name = "AS_OF",
            nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar asOf;

    // @Column(name = "MESSAGE", columnDefinition="CLOB") 
    // @Lob
    @Column(name = "MESSAGE", length=2147483647)
    private String message;

    public JpaCurrentStateId getId() {
        return id;
    }

    public void setId(final JpaCurrentStateId id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public Calendar getAsOf() {
        return asOf;
    }

    public void setAsOf(final Calendar asOf) {
        this.asOf = asOf;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
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
        JpaCurrentState rhs = (JpaCurrentState) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.state, rhs.state)
                .append(this.asOf, rhs.asOf)
                .append(this.message, rhs.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(state)
                .append(asOf)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("state", state)
                .append("asOf", asOf)
                .append("message", message)
                .toString();
    }
}
