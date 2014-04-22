package com.siemens.cto.aem.persistence.jpa.domain;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "jvm_control_history")
public class JpaJvmControlHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jvmId")
    public Long jvmId;

    @Column(name = "controlOperation")
    public String controlOperation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "requestedDate")
    public Calendar requestedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completedDate")
    public Calendar completedDate;

    @Column(name = "requestedBy")
    public String requestedBy;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getJvmId() {
        return jvmId;
    }

    public void setJvmId(final Long jvmId) {
        this.jvmId = jvmId;
    }

    public String getControlOperation() {
        return controlOperation;
    }

    public void setControlOperation(final String controlOperation) {
        this.controlOperation = controlOperation;
    }

    public Calendar getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(final Calendar requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Calendar getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(final Calendar completedDate) {
        this.completedDate = completedDate;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(final String requestedBy) {
        this.requestedBy = requestedBy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JpaJvmControlHistory that = (JpaJvmControlHistory) o;

        if (completedDate != null ? !completedDate.equals(that.completedDate) : that.completedDate != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (jvmId != null ? !jvmId.equals(that.jvmId) : that.jvmId != null) {
            return false;
        }
        if (requestedBy != null ? !requestedBy.equals(that.requestedBy) : that.requestedBy != null) {
            return false;
        }
        if (requestedDate != null ? !requestedDate.equals(that.requestedDate) : that.requestedDate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (jvmId != null ? jvmId.hashCode() : 0);
        result = 31 * result + (requestedDate != null ? requestedDate.hashCode() : 0);
        result = 31 * result + (completedDate != null ? completedDate.hashCode() : 0);
        result = 31 * result + (requestedBy != null ? requestedBy.hashCode() : 0);
        return result;
    }
}
