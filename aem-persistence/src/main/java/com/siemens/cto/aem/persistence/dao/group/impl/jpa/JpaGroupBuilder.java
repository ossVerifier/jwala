package com.siemens.cto.aem.persistence.dao.group.impl.jpa;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.domain.JpaGroup;

public class JpaGroupBuilder {

    private JpaGroup group;

    public JpaGroupBuilder() {
    }

    public JpaGroupBuilder(final JpaGroup aGroup) {
        group = aGroup;
    }

    public JpaGroupBuilder setGroup(final JpaGroup aGroup) {
        group = aGroup;
        return this;
    }

    public Group build() {
    	if(group == null) { 
    		return null;
    	}
        return new Group(new Identifier<Group>(group.getId()),
                         group.getName());
    }
}
