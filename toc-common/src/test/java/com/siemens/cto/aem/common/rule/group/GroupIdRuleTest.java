package com.siemens.cto.aem.common.rule.group;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.rule.AbstractIdRuleTest;
import com.siemens.cto.aem.common.rule.Rule;

public class GroupIdRuleTest extends AbstractIdRuleTest {

    @Override
    protected Rule createValidRule() {
        return new GroupIdRule(new Identifier<Group>(1L));
    }

    @Override
    protected Rule createInvalidRule() {
        return new GroupIdRule(null);
    }
}
