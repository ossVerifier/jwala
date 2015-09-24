package com.siemens.cto.aem.persistence.jpa.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaGroupControlHistory that = (JpaGroupControlHistory) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(groupId, that.groupId)
                .append(controlOperation, that.controlOperation)
                .append(requestedDate, that.requestedDate)
                .append(completedDate, that.completedDate)
                .append(requestedBy, that.requestedBy)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(groupId)
                .append(controlOperation)
                .append(requestedDate)
                .append(completedDate)
                .append(requestedBy)
                .toHashCode();
    }
}
