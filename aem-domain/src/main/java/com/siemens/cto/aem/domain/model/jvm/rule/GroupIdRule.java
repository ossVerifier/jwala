package com.siemens.cto.aem.domain.model.jvm.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;

public class GroupIdRule implements Rule {

    private final Identifier<Group> groupId;

    public GroupIdRule(final Identifier<Group> theId) {
        groupId = theId;
    }

    @Override
    public boolean isValid() {
        return (groupId != null);
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.GROUP_NOT_SPECIFIED,
                                          "Group Id was not specified");
        }
    }
}
