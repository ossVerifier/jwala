package com.siemens.cto.aem.domain.model.rule.group;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;

public class GroupIdsRuleTest {

    @Test
    public void testCreateRuleIdentifierOfGroup() {
        final Set<Identifier<Group>> groupIds = new TreeSet<Identifier<Group>>();
        final GroupIdsRule gir = new GroupIdsRule(groupIds);
        final Long id = new Long(0);
        final Identifier<Group> anId = new Identifier<Group>(id);
        final Rule rule = gir.createRule(anId);
        assertTrue(rule.isValid());
    }
}
