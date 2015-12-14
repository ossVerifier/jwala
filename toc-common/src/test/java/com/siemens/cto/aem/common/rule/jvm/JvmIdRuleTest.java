package com.siemens.cto.aem.common.rule.jvm;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.rule.AbstractIdRuleTest;
import com.siemens.cto.aem.common.rule.Rule;

public class JvmIdRuleTest extends AbstractIdRuleTest {

    @Override
    protected Rule createValidRule() {
        return new JvmIdRule(new Identifier<Jvm>(1L));
    }

    @Override
    protected Rule createInvalidRule() {
        return new JvmIdRule(null);
    }
}
