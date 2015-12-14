package com.siemens.cto.aem.common.rule.jvm;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.rule.Rule;
import com.siemens.cto.aem.common.rule.identifier.MultipleIdentifiersRule;

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
