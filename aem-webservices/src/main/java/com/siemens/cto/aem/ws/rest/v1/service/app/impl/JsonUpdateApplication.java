package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JsonUpdateApplication {

    private Long   webappId;
    private String name;
    private Long   groupId;
    private String webappContext;
    
    public JsonUpdateApplication() {  
    }

    public JsonUpdateApplication(Long groupId, String name, String webappContext, Long webappId) {
        this.groupId = groupId;
        this.webappId = webappId;
        this.name = name;
        this.webappContext = webappContext;
    }

    public UpdateApplicationCommand toUpdateCommand() {
        return  new UpdateApplicationCommand(
                    Identifier.id(webappId, Application.class),
                    Identifier.id(groupId, Group.class), webappContext, name);
    }

    public Long getWebappId() {
        return webappId;
    }

    public void setWebappId(Long webappId) {
        this.webappId = webappId;
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
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getGroupId()).append(getName()).append(getWebappContext()).append(getWebappId()).toHashCode();
    }

    @Override
    public Object clone( ) {
        return new JsonUpdateApplication(
                getGroupId(),
                getName(),
                getWebappContext(),
                getWebappId() );
    }
    
    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);        
    }
    
    /* test code:
     * assertEquals(testJua,testJua.clone())
     * assertEquals(testJua.hashCode(),testJua.clone().hashCode())
     */

}