package com.siemens.cto.aem.common.rule.group;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.rule.Rule;
import com.siemens.cto.aem.common.rule.identifier.MultipleIdentifiersRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GroupIdsRule extends MultipleIdentifiersRule<Group> implements Rule {

    public GroupIdsRule(final Collection<Identifier<Group>> theGroupIds) {
        this(new HashSet<>(theGroupIds));
    }

    public GroupIdsRule(final Set<Identifier<Group>> theGroupIds) {
        super(theGroupIds);
    }

    @Override
    protected Rule createRule(final Identifier<Group> anId) {
        return new GroupIdRule(anId);
    }
}
