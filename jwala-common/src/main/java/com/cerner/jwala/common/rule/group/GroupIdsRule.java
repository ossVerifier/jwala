package com.cerner.jwala.common.rule.group;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.rule.Rule;
import com.cerner.jwala.common.rule.identifier.MultipleIdentifiersRule;

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
