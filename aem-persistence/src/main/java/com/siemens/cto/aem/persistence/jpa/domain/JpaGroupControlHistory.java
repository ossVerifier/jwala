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

@Entity(name = "group_control_history")
public class JpaGroupControlHistory implements Serializable {

    public static final int MAX_OUTPUT_LENGTH = 2048;

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "groupId")
    public Long groupId;

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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getControlOperation() {
        return controlOperation;
    }

    public void setControlOperation(String controlOperation) {
        this.controlOperation = controlOperation;
    }

    public Calendar getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(Calendar requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Calendar getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Calendar completedDate) {
        this.completedDate = completedDate;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((completedDate == null) ? 0 : completedDate.hashCode());
        result = prime * result + ((controlOperation == null) ? 0 : controlOperation.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((requestedBy == null) ? 0 : requestedBy.hashCode());
        result = prime * result + ((requestedDate == null) ? 0 : requestedDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JpaGroupControlHistory other = (JpaGroupControlHistory) obj;
        if (completedDate == null) {
            if (other.completedDate != null)
                return false;
        } else if (!completedDate.equals(other.completedDate))
            return false;
        if (controlOperation == null) {
            if (other.controlOperation != null)
                return false;
        } else if (!controlOperation.equals(other.controlOperation))
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (requestedBy == null) {
            if (other.requestedBy != null)
                return false;
        } else if (!requestedBy.equals(other.requestedBy))
            return false;
        if (requestedDate == null) {
            if (other.requestedDate != null)
                return false;
        } else if (!requestedDate.equals(other.requestedDate))
            return false;
        return true;
    }

}
