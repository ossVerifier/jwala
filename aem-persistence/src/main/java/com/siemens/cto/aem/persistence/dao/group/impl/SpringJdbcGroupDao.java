package com.siemens.cto.aem.persistence.dao.group.impl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.CreateGroupEvent;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;

public class SpringJdbcGroupDao implements GroupDao {

    private final NamedParameterJdbcTemplate template;

    public SpringJdbcGroupDao(final DataSource theDataSource) {
        template = new NamedParameterJdbcTemplate(theDataSource);
    }

    @Override
    public Group createGroup(final CreateGroupEvent aGroupToCreate) {

        final String insert = "INSERT INTO GRP(NAME, CREATEDATE, CREATEBY) VALUES (" + QueryKey.NAME.getQueryKeyPlus() +
                                                                                       QueryKey.CREATED_DATE.getQueryKeyPlus() +
                                                                                       QueryKey.CREATED_BY.getQueryKey() + ")";

        final SqlParameterSource input = new MapSqlParameterSource().addValue(QueryKey.NAME.getKey(), aGroupToCreate.getCreateGroupCommand().getGroupName())
                                                                    .addValue(QueryKey.CREATED_DATE.getKey(), aGroupToCreate.getAuditEvent().getDateTime().getDate())
                                                                    .addValue(QueryKey.CREATED_BY.getKey(), aGroupToCreate.getAuditEvent().getUser().getUserId());

        final KeyHolder insertedKey = new GeneratedKeyHolder();

        final int insertCount = template.update(insert,
                                                input,
                                                insertedKey);

        if (insertCount == 1) {
            return getGroup(new Identifier<Group>(insertedKey.getKey().longValue()));
        } else {
            throw new RuntimeException("Insert failed for GroupCreation " + aGroupToCreate);
        }
    }

    @Override
    public Group updateGroup(final UpdateGroupEvent aGroupToUpdate) {

        final String update = "UPDATE GRP SET NAME = " + QueryKey.NAME.getQueryKeyPlus() +
                                             "LASTUPDATEDATE = " + QueryKey.UPDATED_DATE.getQueryKeyPlus() +
                                             "UPDATEBY = " + QueryKey.UPDATED_BY.getQueryKey() + " " +
                              "WHERE ID = " + QueryKey.ID.getQueryKey();

        final SqlParameterSource input = new MapSqlParameterSource().addValue(QueryKey.NAME.getKey(), aGroupToUpdate.getUpdateGroupCommand().getNewName())
                                                                    .addValue(QueryKey.UPDATED_DATE.getKey(), aGroupToUpdate.getAuditEvent().getDateTime().getDate())
                                                                    .addValue(QueryKey.UPDATED_BY.getKey(), aGroupToUpdate.getAuditEvent().getUser().getUserId())
                                                                    .addValue(QueryKey.ID.getKey(), aGroupToUpdate.getUpdateGroupCommand().getId().getId());

        final int updateCount = template.update(update,
                                                input);

        if (updateCount == 1) {
            return getGroup(aGroupToUpdate.getUpdateGroupCommand().getId());
        } else {
            throw new RuntimeException("Update failed for GroupUpdate " + aGroupToUpdate);
        }
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException {

        try {
            final String select = "SELECT ID, NAME FROM GRP WHERE ID = " + QueryKey.ID.getQueryKey();

            final SqlParameterSource input = new MapSqlParameterSource(QueryKey.ID.getKey(), aGroupId.getId());

            final Group group = template.queryForObject(select,
                                                        input,
                                                        new GroupRowMapper());

            return group;
        } catch (final EmptyResultDataAccessException erdae) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                                        "Group not found: " + aGroupId);
        }
    }

    @Override
    public List<Group> getGroups(final PaginationParameter somePagination) {

        final String select = "SELECT ID, NAME FROM GRP ORDER BY ID ASC " +
                              "LIMIT " + QueryKey.LIMIT.getQueryKey() + " OFFSET " + QueryKey.OFFSET.getQueryKey();

        final SqlParameterSource input = new MapSqlParameterSource().addValue(QueryKey.OFFSET.getKey(), somePagination.getOffset())
                                                                    .addValue(QueryKey.LIMIT.getKey(), somePagination.getLimit());

        final List<Group> groups = template.query(select,
                                                  input,
                                                  new GroupRowMapper());

        return groups;
    }

    @Override
    public List<Group> findGroups(final String aName,
                                  final PaginationParameter somePagination) {

        final String select = "SELECT ID, NAME FROM GRP WHERE NAME LIKE " + QueryKey.NAME.getQueryKey() + " " +
                              "ORDER BY NAME ASC " +
                              "LIMIT " + QueryKey.LIMIT.getQueryKey() + " OFFSET " + QueryKey.OFFSET.getQueryKey();

        final SqlParameterSource input = new MapSqlParameterSource().addValue(QueryKey.NAME.getKey(), "%" + aName + "%")
                                                                    .addValue(QueryKey.OFFSET.getKey(), somePagination.getOffset())
                                                                    .addValue(QueryKey.LIMIT.getKey(), somePagination.getLimit());

        final List<Group> groups = template.query(select,
                                                  input,
                                                  new GroupRowMapper());

        return groups;
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) {

        //TODO We should consider whether we support hard/soft deletes (this will affect method signature similar to create/update)
        final String delete = "DELETE FROM GRP WHERE ID=" + QueryKey.ID.getQueryKey();

        final SqlParameterSource input = new MapSqlParameterSource(QueryKey.ID.getKey(), aGroupId.getId());

        final int deleteCount = template.update(delete,
                                                input);

        if (deleteCount > 1) {
            throw new RuntimeException("Unknown problem when deleting Group " + aGroupId);
        }
    }

    private enum QueryKey {
        CREATED_DATE("CREATED_DATE"),
        CREATED_BY("CREATED_BY"),
        ID("ID"),
        NAME("NAME"),
        OFFSET("OFFSET"),
        LIMIT("LIMIT"),
        UPDATED_BY("UPDATED_BY"),
        UPDATED_DATE("UPDATED_DATE");

        private final String key;

        private QueryKey(final String theKey) {
            key = theKey;
        }

        private String getKey() {
            return key;
        }

        private String getQueryKey() {
            return ":" + getKey();
        }

        private String getQueryKeyPlus() {
            return getQueryKey() + ", ";
        }
    }
}
