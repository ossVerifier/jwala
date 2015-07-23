package com.siemens.cto.aem.persistence.jpa.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;

@Entity(name = "jvm_control_history")
public class JpaJvmControlHistory implements Serializable {

    public static final int MAX_OUTPUT_LENGTH = 1048576;

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

    @Column(name = "returnCode")
    public Integer returnCode;

    @Column(name = "returnOutput", length = MAX_OUTPUT_LENGTH)
    public String returnOutput;

    @Column(name = "returnErrorOutput", length = MAX_OUTPUT_LENGTH)
    public String returnErrorOutput;

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

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(final Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnOutput() {
        return returnOutput;
    }

    public void setReturnOutput(final String returnOutput) {
        this.returnOutput = returnOutput;
    }

    public String getReturnErrorOutput() {
        return returnErrorOutput;
    }

    public void setReturnErrorOutput(final String returnErrorOutput) {
        this.returnErrorOutput = returnErrorOutput;
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
        if (controlOperation != null ? !controlOperation.equals(that.controlOperation) : that.controlOperation != null) {
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
        if (returnCode != null ? !returnCode.equals(that.returnCode) : that.returnCode != null) {
            return false;
        }
        if (returnErrorOutput != null ? !returnErrorOutput.equals(that.returnErrorOutput) : that.returnErrorOutput != null) {
            return false;
        }
        if (returnOutput != null ? !returnOutput.equals(that.returnOutput) : that.returnOutput != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (jvmId != null ? jvmId.hashCode() : 0);
        result = 31 * result + (controlOperation != null ? controlOperation.hashCode() : 0);
        result = 31 * result + (requestedDate != null ? requestedDate.hashCode() : 0);
        result = 31 * result + (completedDate != null ? completedDate.hashCode() : 0);
        result = 31 * result + (requestedBy != null ? requestedBy.hashCode() : 0);
        result = 31 * result + (returnCode != null ? returnCode.hashCode() : 0);
        result = 31 * result + (returnOutput != null ? returnOutput.hashCode() : 0);
        result = 31 * result + (returnErrorOutput != null ? returnErrorOutput.hashCode() : 0);
        return result;
    }
}
