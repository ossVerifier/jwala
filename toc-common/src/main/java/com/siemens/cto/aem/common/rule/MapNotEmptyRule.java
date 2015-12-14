package com.siemens.cto.aem.common.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;

import java.util.Map;

/**
 * Created by z003e5zv on 3/17/2015.
 */
public class MapNotEmptyRule implements Rule {

    public Map<?,?> mapToTest;
    public MapNotEmptyRule(Map<?,?> mapToTest) {
        this.mapToTest = mapToTest;
    }
    @Override
    public boolean isValid() {
        if (mapToTest == null || mapToTest.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.RESOURCE_INSTANCE_MAP_NOT_INCLUDED, "Resource Instance Attributes not found");
        }
    }
}
