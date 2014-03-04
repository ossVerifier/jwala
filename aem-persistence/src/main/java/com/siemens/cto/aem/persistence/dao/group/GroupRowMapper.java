package com.siemens.cto.aem.persistence.dao.group;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class GroupRowMapper implements RowMapper<Group> {

    @Override
    public Group mapRow(final ResultSet rs,
                        final int rowNum) throws SQLException {
        return new Group(new Identifier<Group>(rs.getLong(1)),
                         rs.getString(2));
    }
}
