package com.siemens.cto.aem.domain.model.rule.resource;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.rule.Rule;

/**
 * Created by z003e5zv on 3/17/2015.
 */
public class ResourceInstanceParentRule implements Rule {

    private Long parentId;
    private String parentType;

    public ResourceInstanceParentRule(Long parentId, String parentType) {
        this.parentId = parentId;
        this.parentType = parentType;
    }

    @Override
    public boolean isValid() {
        if (this.parentId == null || this.parentType == null || this.parentId < 0 || "".equals(this.parentType)) {
            return false;
        }
        return true;
    }

    @Override
    public void validate() throws BadRequestException {
        //todo: this should check to see if the parent type matched to an existing parent id, if not then throw an appropriate error
    }
}
