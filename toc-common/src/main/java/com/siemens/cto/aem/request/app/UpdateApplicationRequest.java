package com.siemens.cto.aem.request.app;

import com.siemens.cto.aem.request.Request;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.app.ApplicationContextRule;
import com.siemens.cto.aem.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.rule.app.ApplicationNameRule;
import com.siemens.cto.aem.rule.group.GroupIdRule;

import java.io.Serializable;

public class UpdateApplicationRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final Identifier<Application> id;
    private final Identifier<Group> newGroupId;
    private final String newWebAppContext;
    private final String newName;
    private final boolean newSecure;
    private final boolean newLoadBalanceAcrossServers;

    public UpdateApplicationRequest(
            final Identifier<Application> theId,
            final Identifier<Group> theGroupId,
            final String theNewWebAppContext,
            final String theNewName,
            boolean theNewSecure, boolean theNewLoadBalanceAcrossServers) {
        id = theId;
        newGroupId = theGroupId;
        newName = theNewName;
        newWebAppContext = theNewWebAppContext;
        newSecure = theNewSecure;
        newLoadBalanceAcrossServers = theNewLoadBalanceAcrossServers;
    }

    public Identifier<Application> getId() {
        return id;
    }

    public Identifier<Group> getNewGroupId() {
        return newGroupId;
    }

    public String getNewWebAppContext() {
        return newWebAppContext;
    }
    public String getNewName() {
        return newName;
    }

    public boolean isNewSecure() {
        return newSecure;
    }

    public boolean isNewLoadBalanceAcrossServers() {
        return newLoadBalanceAcrossServers;
    }

    @Override
    public void validate() throws BadRequestException {
        new MultipleRules(new ApplicationIdRule(id),
                                new GroupIdRule(newGroupId),
                                new ApplicationNameRule(newName),
                                new ApplicationContextRule(newWebAppContext)).validate();
    }
}
