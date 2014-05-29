package com.siemens.cto.aem.domain.model.rule;

import java.util.Arrays;
import java.util.List;

import com.siemens.cto.aem.common.exception.BadRequestException;

public class MultipleRules implements Rule {

    private final List<Rule> rules;

    public MultipleRules(final List<Rule> someRules) {
        rules = someRules;
    }

    public MultipleRules(final Rule... someRules) {
        this(Arrays.asList(someRules));
    }

    @Override
    public void validate() throws BadRequestException {
        for (final Rule rule : rules) {
            rule.validate();
        }
    }

    @Override
    public boolean isValid() {
        for (final Rule rule : rules) {
            if(!rule.isValid()) { 
                return false; 
            }
        }
        return true;
    }

}
