package com.siemens.cto.aem.service.app;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.app.ControlApplicationRequest;
import com.siemens.cto.aem.exception.CommandFailureException;

/**
 * An interface that defines application-centric external command tasks.
 * <p/>
 * Created by z003bpej on 9/9/2015.
 */
public interface ApplicationCommandService {
    CommandOutput controlApplication(ControlApplicationRequest applicationRequest, Application app, String... params) throws CommandFailureException;
}
