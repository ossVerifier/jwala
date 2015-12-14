package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

public class JpaLiteGroupBuilder {

    private JpaGroup group;

    public JpaLiteGroupBuilder() {
    }

    public JpaLiteGroupBuilder(final JpaGroup aGroup) {
        group = aGroup;
    }

    public JpaLiteGroupBuilder setGroup(final JpaGroup aGroup) {
        group = aGroup;
        return this;
    }

    public LiteGroup build() {
        return new LiteGroup(new Identifier<Group>(group.getId()),
                             group.getName());
    }
}
