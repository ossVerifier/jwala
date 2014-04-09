package com.siemens.cto.aem.domain.model.rule.jvm;

import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.Rule;
import com.siemens.cto.aem.domain.model.rule.identifier.IdentifierRule;

public class JvmIdRule extends IdentifierRule<Jvm> implements Rule {

    public JvmIdRule(final Identifier<Jvm> theId) {
        super(theId,
              AemFaultType.JVM_NOT_SPECIFIED,
              "JVM Id was not specified");
    }
}
