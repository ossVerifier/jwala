package com.siemens.cto.aem.request.group;

import com.siemens.cto.aem.request.Request;

public interface GroupRequest extends Request {
    
    Long getId();
    
    String getExternalOperationName();
    
    String getType();

}
