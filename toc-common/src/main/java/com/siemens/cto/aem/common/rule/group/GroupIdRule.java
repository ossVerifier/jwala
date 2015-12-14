package com.siemens.cto.aem.common.rule.group;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.rule.Rule;
import com.siemens.cto.aem.common.rule.identifier.IdentifierRule;

public class GroupIdRule extends IdentifierRule<Group> implements Rule {

    public GroupIdRule(final Identifier<Group> theId) {
        super(theId,
              AemFaultType.GROUP_NOT_SPECIFIED,
              "Group Id was not specified");
    }
}
