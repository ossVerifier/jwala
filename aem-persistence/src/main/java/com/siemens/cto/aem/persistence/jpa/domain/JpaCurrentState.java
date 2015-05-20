package com.siemens.cto.aem.persistence.jpa.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "current_state")
@NamedQueries({
    @NamedQuery(name = JpaCurrentState.FIND_STALE_STATES_QUERY,
            query = "SELECT j FROM JpaCurrentState j where j.asOf < :cutoff"),
    @NamedQuery(name = JpaCurrentState.UPDATE_STALE_STATES_QUERY,
            query = "update JpaCurrentState j SET j.state = :stateName where j.asOf < :cutoff"),
    @NamedQuery(name = JpaCurrentState.FIND_STALE_STATES_SUBSET_QUERY,
            query = "SELECT j FROM JpaCurrentState j where j.asOf < :cutoff and j.state in :checkStates"),
    @NamedQuery(name = JpaCurrentState.UPDATE_STALE_STATES_SUBSET_QUERY,
            query = "update JpaCurrentState j SET j.state = :stateName where j.asOf < :cutoff and j.state in :checkStates"),
})
public class JpaCurrentState implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String FIND_STALE_STATES_QUERY = "states.findStaleStates";
    public static final String UPDATE_STALE_STATES_QUERY = "states.updateStaleStates";
    public static final String FIND_STALE_STATES_SUBSET_QUERY = "states.subsetFindStaleStates";
    public static final String UPDATE_STALE_STATES_SUBSET_QUERY = "states.subsetUpdateStaleStates";
    public static final String CUTOFF = "cutoff";
    public static final String STATE_NAME = "stateName";
    public static final String CHECK_STATES = "checkStates";
    
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
