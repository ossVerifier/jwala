package com.siemens.cto.aem.domain.model.rule.identifier;

import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;

public abstract class MultipleIdentifiersRule<T> implements Rule {

    private final Set<Rule> rules;

    public MultipleIdentifiersRule(final Set<Identifier<T>> theIds) {
        rules = new HashSet<>();
        for (final Identifier<T> id : theIds) {
            rules.add(createRule(id));
        }
    }

    @Override
    public boolean isValid() {
        for (final Rule rule : rules) {
            if (!rule.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void validate() throws BadRequestException {
        for (final Rule rule : rules) {
            rule.validate();
        }
    }

    protected abstract Rule createRule(final Identifier<T> anId);
}
