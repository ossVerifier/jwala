package com.siemens.cto.aem.domain.model.rule.group;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;

public class GroupIdsRule implements Rule {

    private final Set<Rule> groupIdRules;

    public GroupIdsRule(final Collection<Identifier<Group>> theGroupIds) {
        this(new HashSet<>(theGroupIds));
    }

    public GroupIdsRule(final Set<Identifier<Group>> theGroupIds) {
        groupIdRules = new HashSet<>();
        for (final Identifier<Group> groupId : theGroupIds) {
            groupIdRules.add(new GroupIdRule(groupId));
        }
    }

    @Override
    public boolean isValid() {
        for (final Rule rule : groupIdRules) {
            if (!rule.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void validate() throws BadRequestException {
        for (final Rule rule : groupIdRules) {
            rule.validate();
        }
    }
}
