package com.siemens.cto.aem.common.rule.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.rule.Rule;

public class JvmStateRule implements Rule {

    private final JvmState jvmState;

    public JvmStateRule(final JvmState theJvmState) {
        jvmState = theJvmState;
    }

    @Override
    public boolean isValid() {
        return jvmState != null;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.JVM_STATE_NOT_SPECIFIED,
                                          "A non-null JVM State was not specified");
        }
    }
}
