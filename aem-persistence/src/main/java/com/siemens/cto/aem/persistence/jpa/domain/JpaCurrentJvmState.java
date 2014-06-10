package com.siemens.cto.aem.persistence.jpa.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "current_jvm_state")
public class JpaCurrentJvmState implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "STATE",
            nullable = false)
    private String state;

    @Column(name = "AS_OF",
            nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar asOf;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JpaCurrentJvmState that = (JpaCurrentJvmState) o;

        if (asOf != null ? !asOf.equals(that.asOf) : that.asOf != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (state != null ? !state.equals(that.state) : that.state != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (asOf != null ? asOf.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JpaCurrentJvmState{" +
               "id=" + id +
               ", state='" + state + '\'' +
               ", asOf=" + asOf +
               '}';
    }
}
