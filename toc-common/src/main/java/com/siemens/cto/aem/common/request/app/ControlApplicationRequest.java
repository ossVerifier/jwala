package com.siemens.cto.aem.common.request.app;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.rule.app.ApplicationIdRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class ControlApplicationRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final Identifier<Application> appId;
    private final ApplicationControlOperation controlOperation;

    public ControlApplicationRequest(final Identifier<Application> theId,
                                     final ApplicationControlOperation theControlOperation) {
        appId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<Application> getAppId() {
        return appId;
    }

    public ApplicationControlOperation getControlOperation() {
        return controlOperation;
    }

    @Override
    public void validate() {
        new ApplicationIdRule(appId).validate();
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
        ControlApplicationRequest rhs = (ControlApplicationRequest) obj;
        return new EqualsBuilder()
                .append(this.appId, rhs.appId)
                .append(this.controlOperation, rhs.controlOperation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(appId)
                .append(controlOperation)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("appId", appId)
                .append("controlOperation", controlOperation)
                .toString();
    }
}
