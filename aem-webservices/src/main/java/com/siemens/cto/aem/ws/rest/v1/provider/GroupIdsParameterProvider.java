package com.siemens.cto.aem.ws.rest.v1.provider;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.group.Group;

public class GroupIdsParameterProvider extends AbstractIdsParameterProvider<Group> {

    @QueryParam("groupId")
    private Set<String> groupIds;

    public GroupIdsParameterProvider(final Set<String> someGroupIds) {
        this();
        groupIds = new HashSet<>(someGroupIds);
    }

    public GroupIdsParameterProvider() {
        super("Invalid Group Identifier specified");
    }

    @Override
    protected Set<String> getIds() {
        return groupIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupIds", groupIds)
                .toString();
    }
}
