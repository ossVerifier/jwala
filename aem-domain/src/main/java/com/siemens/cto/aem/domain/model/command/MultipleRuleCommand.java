package com.siemens.cto.aem.domain.model.command;

import java.util.Arrays;
import java.util.List;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.rule.Rule;

public class MultipleRuleCommand implements Command {

    private final List<Rule> rules;

    public MultipleRuleCommand(final List<Rule> someRules) {
        rules = someRules;
    }

    public MultipleRuleCommand(final Rule... someRules) {
        this(Arrays.asList(someRules));
    }

    @Override
    public void validateCommand() throws BadRequestException {
        for (final Rule rule : rules) {
            rule.validate();
        }
    }
}
