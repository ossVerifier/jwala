package com.siemens.cto.aem.common.request.group;

import com.siemens.cto.aem.common.request.Request;

public interface GroupRequest extends Request {
    
    Long getId();
    
    String getExternalOperationName();
    
    String getType();

}
