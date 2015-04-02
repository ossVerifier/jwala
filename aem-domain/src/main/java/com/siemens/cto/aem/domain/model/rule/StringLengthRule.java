package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;

/**
 * Created by z003e5zv on 3/30/2015.
 */
public class StringLengthRule implements  Rule {

    private final Integer minLength;
    private final Integer maxLength;
    private final String stringToEvaluate;
    public StringLengthRule(Integer minimumLength, Integer maxLength, String stringToEvaluate) {
        if (minimumLength == null || minimumLength < 1) {
            this.minLength = 1;
        }
        else {
            this.minLength = minimumLength;
        }
        if (minimumLength == null || this.minLength > maxLength) {
            this.maxLength = Integer.MAX_VALUE;
        }
        else {
            this.maxLength = maxLength;
        }
        this.stringToEvaluate = stringToEvaluate;


    }
    @Override
    public boolean isValid() {
        if (this.stringToEvaluate == null || this.stringToEvaluate.length() < this.minLength || this.stringToEvaluate.length() > this.maxLength) {
            return false;
        }
        return true;
    }

    @Override
    public void validate() throws BadRequestException {

    }
}
