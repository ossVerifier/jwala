package com.siemens.cto.aem.request;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.rule.Rule;

import java.util.Arrays;
import java.util.List;

public class MultipleRuleRequest implements Request {

    private final List<Rule> rules;

    public MultipleRuleRequest(final List<Rule> someRules) {
        rules = someRules;
    }

    public MultipleRuleRequest(final Rule... someRules) {
        this(Arrays.asList(someRules));
    }

    @Override
    public void validate() throws BadRequestException {
        for (final Rule rule : rules) {
            rule.validate();
        }
    }
}
