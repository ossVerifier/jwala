package com.siemens.cto.aem.domain.model.rule.app;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;
import com.siemens.cto.aem.domain.model.rule.identifier.IdentifierRule;

public class ApplicationIdRule extends IdentifierRule<Application> implements Rule {

    public ApplicationIdRule(final Identifier<Application> theId) {
        super(theId,
              AemFaultType.APPLICATION_NOT_SPECIFIED,
              "Application Id was not specified");
    }

}
