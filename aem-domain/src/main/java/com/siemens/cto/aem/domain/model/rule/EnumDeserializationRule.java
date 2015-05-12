package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;

/**
 * Created by z003e5zv on 3/17/2015.
 */
public class EnumDeserializationRule<T extends Enum<T>> implements Rule {

    private T[] enumValuesToAllow;
    private String nameToTest;
    public EnumDeserializationRule(String nameToTest, T[] enumValuesToAllow) {
        this.enumValuesToAllow = enumValuesToAllow.clone();
    }
    public EnumDeserializationRule(String nameToTest, Class<T> enumValuesToAllow) {
        this.enumValuesToAllow = enumValuesToAllow.getEnumConstants();
    }

    @Override
    public boolean isValid() {
        for (T validEnum: enumValuesToAllow) {
            if (this.nameToTest.equalsIgnoreCase(validEnum.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validate() throws BadRequestException {

    }
}
