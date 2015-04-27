package com.siemens.cto.aem.domain.model.app;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class Application {
    
    private Identifier<Application> id;
    
    private Group group;
    
    private String webAppContext;

    private String name;

    private String warPath;

    private boolean secure;

    private boolean loadBalanceAcrossServers;

    public Application(Identifier<Application> anId,
                       String aName,
                       String aWarPath,
                       String aWebAppContext,
                       Group aGroup,
                       boolean secure,
                       boolean loadBalanceAcrossServers) {
        group = aGroup;
        id = anId;
        webAppContext = aWebAppContext;
        warPath = aWarPath;
        name = aName;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getWebAppContext() {
        return webAppContext;
    }

    public void setWebAppContext(String webAppContext) {
        this.webAppContext = webAppContext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWarPath() {
        return warPath;
    }

    public void setWarPath(String warPath) {
        this.warPath = warPath;
    }

    public Identifier<Application> getId() {
        return id;
    }

    public void setId(Identifier<Application> id) {
        this.id = id;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isLoadBalanceAcrossServers() {
        return loadBalanceAcrossServers;
    }

    public void setLoadBalanceAcrossServers(boolean loadBalanceAcrossServers) {
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
    }

}
