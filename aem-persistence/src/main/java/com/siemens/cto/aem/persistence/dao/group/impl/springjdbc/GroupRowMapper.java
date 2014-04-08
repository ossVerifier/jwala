package com.siemens.cto.aem.persistence.dao.group.impl.springjdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import org.springframework.jdbc.core.RowMapper;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class GroupRowMapper implements RowMapper<Group> {

    @Override
    public Group mapRow(final ResultSet rs,
                        final int rowNum) throws SQLException {
        return new Group(new Identifier<Group>(rs.getLong(1)),
                         rs.getString(2),
                         Collections.<Jvm>emptySet()); //TODO This needs to be fixed if we decide to keep this code around
    }
}
