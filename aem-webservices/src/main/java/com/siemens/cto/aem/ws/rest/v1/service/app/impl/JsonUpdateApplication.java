package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class JsonUpdateApplication {

    private Long   webappId;
    private String name;
    private Long   groupId;
    private String webappContext;
    
    public JsonUpdateApplication() {  }

    public JsonUpdateApplication(Long groupId2, String name2, String webappContext2, Long webappId2) {
        setGroupId(groupId2);
        setWebappId(webappId2);
        setName(name2);
        setWebappContext(webappContext2);
    }

    public UpdateApplicationCommand toUpdateCommand() throws BadRequestException {
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
     * `assertEquals(testJua,testJua.clone())
     */

}