package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.common.domain.model.state.StateType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Embeddable
public class JpaCurrentStateId implements Serializable {

    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPE")
    @Enumerated(value = EnumType.STRING)
    private StateType stateType;

    public JpaCurrentStateId() {
    }

    public JpaCurrentStateId(final Long anId,
                             final StateType aStateType) {
        id = anId;
        stateType = aStateType;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public StateType getStateType() {
        return stateType;
    }

    public void setStateType(final StateType stateType) {
        this.stateType = stateType;
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
        JpaCurrentStateId rhs = (JpaCurrentStateId) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.stateType, rhs.stateType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(stateType)
                .toHashCode();
    }
}
