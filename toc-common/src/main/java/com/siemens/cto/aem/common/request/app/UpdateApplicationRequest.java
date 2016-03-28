package com.siemens.cto.aem.common.request.app;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.app.ApplicationContextRule;
import com.siemens.cto.aem.common.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.common.rule.app.ApplicationNameRule;
import com.siemens.cto.aem.common.rule.group.GroupIdRule;

import java.io.Serializable;

public class UpdateApplicationRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final Identifier<Application> id;
    private final Identifier<Group> newGroupId;
    private final String newWebAppContext;
    private final String newName;
    private final boolean newSecure;
    private final boolean newLoadBalanceAcrossServers;
    private final boolean unpackWar;

    public UpdateApplicationRequest(
            final Identifier<Application> theId,
            final Identifier<Group> theGroupId,
            final String theNewWebAppContext,
            final String theNewName,
            boolean theNewSecure, boolean theNewLoadBalanceAcrossServers, boolean unpackWar) {
        id = theId;
        newGroupId = theGroupId;
        newName = theNewName;
        newWebAppContext = theNewWebAppContext;
        newSecure = theNewSecure;
        newLoadBalanceAcrossServers = theNewLoadBalanceAcrossServers;
        this.unpackWar = unpackWar;
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
    public void validate() {
        new MultipleRules(new ApplicationIdRule(id),
                                new GroupIdRule(newGroupId),
                                new ApplicationNameRule(newName),
                                new ApplicationContextRule(newWebAppContext)).validate();
    }

    public boolean isUnpackWar() {
        return unpackWar;
    }
}
