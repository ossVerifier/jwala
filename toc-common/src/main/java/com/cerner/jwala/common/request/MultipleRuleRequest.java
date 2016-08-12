package com.cerner.jwala.common.request;

import java.util.Arrays;
import java.util.List;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.Rule;

public class MultipleRuleRequest implements Request {

    private final List<Rule> rules;

    public MultipleRuleRequest(final List<Rule> someRules) {
        rules = someRules;
    }

    public MultipleRuleRequest(final Rule... someRules) {
        this(Arrays.asList(someRules));
    }

    @Override
    public void validate() {
        for (final Rule rule : rules) {
            rule.validate();
        }
    }
}
