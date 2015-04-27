package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JsonCreateApplication {

    public String name;
    public Long   groupId;
    public String webappContext;
    public boolean secure;
    public boolean loadBalanceAcrossServers;
    
    public JsonCreateApplication() {  }

    public JsonCreateApplication(Long groupId2, String name2, String webappContext2, boolean secure, boolean loadBalanceAcrossServers) {
        setGroupId(groupId2);
        setName(name2);
        setWebappContext(webappContext2);
        setSecure(secure);
        setLoadBalanceAcrossServers(loadBalanceAcrossServers);
    }

    public CreateApplicationCommand toCreateCommand() throws BadRequestException {
        return  new CreateApplicationCommand(
                    Identifier.id(groupId, Group.class),name,webappContext, secure, true);
    }

    
    @Override
    public Object clone( ) {
        return new JsonCreateApplication(
                getGroupId(),
                getName(),
                getWebappContext(),
                isSecure(),
                isLoadBalanceAcrossServers());
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getGroupId())
                                    .append(getName())
                                    .append(getWebappContext())
                                    .append(isSecure())
                                    .append(isLoadBalanceAcrossServers()).toHashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getWebappContext() {
        return webappContext;
    }

    public void setWebappContext(String webappContext) {
        this.webappContext = webappContext;
    }
    
    /* test code:
     * assertEquals(testJca,testJca.clone())
     * assertEquals(testJca.hashCode(),testJca.clone().hashCode())
     */

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
