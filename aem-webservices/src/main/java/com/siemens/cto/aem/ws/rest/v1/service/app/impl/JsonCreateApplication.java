package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class JsonCreateApplication {

    private String name;
    private Long groupId;
    private String webappContext;
    private boolean secure;
    private boolean loadBalanceAcrossServers;

    public JsonCreateApplication() {
    }

    public JsonCreateApplication(Long groupId2, String name2, String webappContext2, boolean secure, boolean loadBalanceAcrossServers) {
        groupId = groupId2;
        name = name2;
        webappContext = webappContext2;
        this.secure = secure;
        this.loadBalanceAcrossServers = loadBalanceAcrossServers;
    }

    public CreateApplicationCommand toCreateCommand() throws BadRequestException {
        return new CreateApplicationCommand(
                Identifier.id(groupId, Group.class), name, webappContext, secure, loadBalanceAcrossServers);
    }


    @Override
    public Object clone() {
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
