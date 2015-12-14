package com.siemens.cto.aem.service.dispatch;

import com.siemens.cto.aem.common.domain.model.id.Identifier;

public interface DispatchNotificationService<T> {

    void notifyCompletion(Identifier<T> identifier);
    
}
