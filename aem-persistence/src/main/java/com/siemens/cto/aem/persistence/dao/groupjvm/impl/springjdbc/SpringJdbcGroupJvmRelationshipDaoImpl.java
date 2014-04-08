package com.siemens.cto.aem.persistence.dao.groupjvm.impl.springjdbc;

import java.util.Date;

import javax.sql.DataSource;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.dao.groupjvm.GroupJvmRelationshipDao;

public class SpringJdbcGroupJvmRelationshipDaoImpl implements GroupJvmRelationshipDao {

    private final NamedParameterJdbcTemplate template;

    public SpringJdbcGroupJvmRelationshipDaoImpl(final DataSource theDataSource) {
        this(new NamedParameterJdbcTemplate(theDataSource));
    }

    public SpringJdbcGroupJvmRelationshipDaoImpl(final NamedParameterJdbcTemplate theTemplate) {
        template = theTemplate;
    }

    @Override
    public void addJvmToGroup(final Event<AddJvmToGroupCommand> aJvmToAdd) {

        try {
            final String insert = "INSERT INTO GRP_JVM(GROUP_ID, JVM_ID, CREATEBY, CREATEDATE, LASTUPDATEDATE, UPDATEBY) VALUES ( " + QueryKey.GROUP_ID.getQueryKeyPlus() +
                                                                                                                                      QueryKey.JVM_ID.getQueryKeyPlus() +
                                                                                                                                      QueryKey.CREATED_BY.getQueryKeyPlus() +
                                                                                                                                      QueryKey.CREATED_DATE.getQueryKeyPlus() +
                                                                                                                                      QueryKey.LAST_UPDATED.getQueryKeyPlus() +
                                                                                                                                      QueryKey.UPDATED_BY.getQueryKey() +
                                                                                                                               ")";

            final String createUser = aJvmToAdd.getAuditEvent().getUser().getUserId();
            final Date createDateTime = aJvmToAdd.getAuditEvent().getDateTime().getDate();

            final SqlParameterSource parameters = new MapSqlParameterSource().addValue(QueryKey.GROUP_ID.getKey(), aJvmToAdd.getCommand().getGroupId().getId())
                                                                             .addValue(QueryKey.JVM_ID.getKey(), aJvmToAdd.getCommand().getJvmId().getId())
                                                                             .addValue(QueryKey.CREATED_BY.getKey(), createUser)
                                                                             .addValue(QueryKey.CREATED_DATE.getKey(), createDateTime)
                                                                             .addValue(QueryKey.UPDATED_BY.getKey(), createUser)
                                                                             .addValue(QueryKey.LAST_UPDATED.getKey(), createDateTime);

            final int insertCount = template.update(insert,
                                                    parameters);

            if (insertCount != 1) {
                throw new RuntimeException("Insert failed for AddJvmToGroup " + aJvmToAdd);
            }

        } catch (final DuplicateKeyException dke) {
            throw new BadRequestException(AemFaultType.JVM_ALREADY_BELONGS_TO_GROUP,
                                          "Jvm: " + aJvmToAdd.getCommand().getJvmId() + " is already assigned to Group: " + aJvmToAdd.getCommand().getGroupId(),
                                          dke);
        }
    }

    @Override
    public void removeJvmFromGroup(final Event<RemoveJvmFromGroupCommand> aJvmToRemove) {

        final String delete = "DELETE FROM GRP_JVM WHERE GROUP_ID = " + QueryKey.GROUP_ID.getQueryKey() +
                              "AND JVM_ID = " + QueryKey.JVM_ID.getQueryKey();

        final SqlParameterSource parameters = new MapSqlParameterSource().addValue(QueryKey.GROUP_ID.getKey(), aJvmToRemove.getCommand().getGroupId().getId())
                                                                         .addValue(QueryKey.JVM_ID.getKey(), aJvmToRemove.getCommand().getJvmId().getId());

        final int deleteCount = template.update(delete,
                                                parameters);

        if (deleteCount > 1) {
            throw new RuntimeException("Unknown problem when removing Jvm from Group " + aJvmToRemove);
        } else if (deleteCount == 0) {
            //TODO Decide whether deleting something that doesn't exist should result in a failure or not
            //TODO Determine what type of message to use since the group, or the jvm, or both could not exist
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                                        "Group not found: " + aJvmToRemove.getCommand().getGroupId());
        }
    }

    @Override
    public void removeRelationshipsForGroup(final Identifier<Group> aGroupId) {

        final String delete = "DELETE FROM GRP_JVM WHERE GROUP_ID = " + QueryKey.GROUP_ID.getQueryKey();

        final SqlParameterSource parameters = new MapSqlParameterSource(QueryKey.GROUP_ID.getKey(), aGroupId);

        final int deleteCount = template.update(delete,
                                                parameters);

        if (deleteCount == 0) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                                        "Group not found: " + aGroupId);
        }
    }

    @Override
    public void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId) {

        final String delete = "DELETE FROM GRP_JVM WHERE JVM_ID = " + QueryKey.JVM_ID.getQueryKey();

        final SqlParameterSource parameters = new MapSqlParameterSource(QueryKey.JVM_ID.getKey(), aJvmId);

        final int deleteCOunt = template.update(delete,
                                                parameters);

        if (deleteCOunt == 0) {
            throw new NotFoundException(AemFaultType.JVM_NOT_FOUND,
                                        "Jvm not found: " + aJvmId);
        }
    }

    private static enum QueryKey {
        GROUP_ID,
        JVM_ID,
        CREATED_BY,
        CREATED_DATE,
        LAST_UPDATED,
        UPDATED_BY;

        public String getKey() {
            return this.name();
        }

        public String getQueryKey() {
            return ":" + getKey() + " ";
        }

        public String getQueryKeyPlus() {
            return getQueryKey() + ", ";
        }
    }
}
