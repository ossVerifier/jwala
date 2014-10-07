package com.siemens.cto.aem.domain.model.rule;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;

public class ShutdownPortNumberRule extends PortNumberRule {

    public ShutdownPortNumberRule(final Integer thePort, final AemFaultType errorCode) {
        super(thePort,
             errorCode,
             false);
    }

    @Override
    public boolean isValid() {
        if(super.getPort() != null && super.getPort() == -1) {
            return true;
        }
        return super.isValid();
    }
}
