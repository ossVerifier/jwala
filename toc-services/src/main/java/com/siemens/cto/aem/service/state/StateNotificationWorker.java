package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;

/**
 * Created by JC043760 on 11/4/2015.
 */
public interface StateNotificationWorker {

    void sendStateChangeNotification(GroupStateService.API groupStateService, CurrentState currentState);

    void refreshState(GroupStateService.API groupStateService, Group group);

}
