package com.siemens.cto.aem.domain.model.rule.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.Rule;

public class JvmIdRule implements Rule {

    private final Identifier<Jvm> jvmId;

    public JvmIdRule(final Identifier<Jvm> theId) {
        jvmId = theId;
    }

    @Override
    public boolean isValid() {
        return (jvmId != null);
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.JVM_NOT_SPECIFIED,
                                          "JVM Id was not specified");
        }
    }
}
