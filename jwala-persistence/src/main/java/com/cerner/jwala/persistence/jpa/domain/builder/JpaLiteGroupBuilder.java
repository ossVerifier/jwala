package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.LiteGroup;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;

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
