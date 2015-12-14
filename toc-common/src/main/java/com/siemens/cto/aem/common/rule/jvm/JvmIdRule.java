package com.siemens.cto.aem.common.rule.jvm;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.rule.Rule;
import com.siemens.cto.aem.common.rule.identifier.IdentifierRule;

public class JvmIdRule extends IdentifierRule<Jvm> implements Rule {

    public JvmIdRule(final Identifier<Jvm> theId) {
        super(theId,
              AemFaultType.JVM_NOT_SPECIFIED,
              "JVM Id was not specified");
    }
}
