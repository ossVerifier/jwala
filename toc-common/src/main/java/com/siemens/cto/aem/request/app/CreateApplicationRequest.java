package com.siemens.cto.aem.request.app;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.request.Request;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.app.ApplicationContextRule;
import com.siemens.cto.aem.rule.app.ApplicationNameRule;
import com.siemens.cto.aem.rule.group.GroupIdRule;

import java.io.Serializable;

public class CreateApplicationRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private String name;
    private String webAppContext;
    private Identifier<Group> groupId;
    private boolean secure;
    private boolean loadBalanceAcrossServers;
    
    public CreateApplicationRequest(Identifier<Group> groupId,
                                    String name,
                                    String webAppContext,
                                    boolean secure,
                                    boolean loadBalanceAcrossServers) {
        this.name = name;
        this.webAppContext = webAppContext;
        this.groupId = groupId;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }
    public String getWebAppContext() {
        return webAppContext;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isLoadBalanceAcrossServers() {
        return loadBalanceAcrossServers;
    }

    @Override
    public void validate() throws BadRequestException {
        new MultipleRules(new GroupIdRule(groupId),
                                new ApplicationNameRule(name),
                                new ApplicationContextRule(webAppContext)).validate();
    }
}
