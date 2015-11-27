package com.siemens.cto.aem.rule.jvm;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.rule.Rule;
import com.siemens.cto.aem.rule.identifier.MultipleIdentifiersRule;

import java.util.Set;

public class JvmIdsRule extends MultipleIdentifiersRule<Jvm> implements Rule {

    public JvmIdsRule(final Set<Identifier<Jvm>> theIds) {
        super(theIds);
    }

    @Override
    protected Rule createRule(final Identifier<Jvm> anId) {
        return new JvmIdRule(anId);
    }
}
