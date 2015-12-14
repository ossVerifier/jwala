package com.siemens.cto.aem.common.rule.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.common.rule.Rule;

public class GroupControlOperationRule implements Rule {

    GroupControlOperation gcOp;
    boolean canStart;
    boolean canStop;
    
    public GroupControlOperationRule(GroupControlOperation gcOp, boolean canStart, boolean canStop) {
        this.gcOp = gcOp;
        this.canStart = canStart;
        this.canStop = canStop;
    }

    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_GROUP_OPERATION;
    }

    @Override
    public boolean isValid() {
        switch(gcOp) {
            case START: return this.canStart;
            case STOP: return this.canStop;
            default: return false;
        }
    }

    @Override
    public void validate() throws BadRequestException {
        if(!isValid()) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_OPERATION, "Invalid group control operation '"+gcOp.getExternalValue()+"'.");
        } 
    }
}
