package com.cerner.jwala.persistence.service.impl;

import com.cerner.jwala.common.domain.model.group.CurrentGroupState;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupState;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.group.*;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.state.SetStateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.builder.JpaGroupBuilder;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupJvmRelationshipService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

public class JpaGroupPersistenceServiceImpl implements GroupPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaGroupPersistenceServiceImpl.class);
    private final GroupCrudService groupCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;


    @PersistenceContext(unitName = "jwala-unit")
    protected EntityManager entityManager; // We're removing the CRUD layer in the near future so going forward, new methods will be using the entity manager in this class.

    public JpaGroupPersistenceServiceImpl(final GroupCrudService theGroupCrudService,
                                          final GroupJvmRelationshipService theGroupJvmRelationshipService) {
        groupCrudService = theGroupCrudService;
        groupJvmRelationshipService = theGroupJvmRelationshipService;
    }

    @Override
    public Group createGroup(final CreateGroupRequest createGroupRequest) {
        final JpaGroup group = groupCrudService.createGroup(createGroupRequest);
        return groupFrom(group, false);
    }

    @Override
    public Group updateGroup(UpdateGroupRequest updateGroupRequest) throws NotFoundException {
        groupCrudService.updateGroup(updateGroupRequest);
        return groupFrom(groupCrudService.getGroup(updateGroupRequest.getId()), false);
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(aGroupId);
        return groupFrom(group, false);
    }

    @Override
    public Group getGroupWithWebServers(final Identifier<Group> aGroupId) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(aGroupId);
        return groupFrom(group, true);
    }

    @Override
    public Group getGroupWithWebServers(final String groupName) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(groupName);
        return groupFrom(group, true);
    }

    @Override
    public Group getGroup(final String name) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(name);
        return groupFrom(group, false);
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId, final boolean fetchWebServers) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(aGroupId);
        return new JpaGroupBuilder(group).setFetchWebServers(fetchWebServers).build();
    }

    @Override
    public List<Group> getGroups() {
        final List<JpaGroup> groups = groupCrudService.getGroups();
        return groupsFrom(groups, false);
    }

    @Override
    public List<Group> getGroups(boolean fetchWebServers) {
        final List<JpaGroup> groups = groupCrudService.getGroups();
        return groupsFrom(groups, fetchWebServers);
    }

    @Override
    public List<Group> findGroups(final String aName) {
        final List<JpaGroup> groups = groupCrudService.findGroups(aName);
        return groupsFrom(groups, false);
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        groupJvmRelationshipService.removeRelationshipsForGroup(aGroupId);
        groupCrudService.removeGroup(aGroupId);
    }

    @Override
    public void removeGroup(final String name) throws NotFoundException {
        removeGroup(new Identifier<Group>(groupCrudService.getGroupId(name)));
    }

    @Override
    public Group addJvmToGroup(AddJvmToGroupRequest addJvmToGroupRequest) throws NotFoundException {
        groupJvmRelationshipService.addJvmToGroup(addJvmToGroupRequest);
        return groupFrom(groupCrudService.getGroup(addJvmToGroupRequest.getGroupId()), false);
    }

    @Override
    public Group removeJvmFromGroup(RemoveJvmFromGroupRequest removeJvmFromGroupRequest) throws NotFoundException {
        groupJvmRelationshipService.removeJvmFromGroup(removeJvmFromGroupRequest);
        return groupFrom(groupCrudService.getGroup(removeJvmFromGroupRequest.getGroupId()), false);
    }

    protected Group groupFrom(final JpaGroup aJpaGroup, final boolean fetchWebServers) {
        return new JpaGroupBuilder(aJpaGroup).setFetchWebServers(fetchWebServers).build();
    }

    protected Group groupFrom(final CurrentState<Group, GroupState> originalStatus, final JpaGroup aJpaGroup) {
        return new JpaGroupBuilder(aJpaGroup).setStateDetail((CurrentGroupState) originalStatus).build();
    }
    protected CurrentState<Group, GroupState> groupStateFrom(final JpaGroup aJpaGroup) {
        return new JpaGroupBuilder(aJpaGroup).build().getCurrentState();
    }

    protected List<Group> groupsFrom(final List<JpaGroup> someJpaGroups, final boolean fetchWebServers) {
        final List<Group> groups = new ArrayList<>();
        for (final JpaGroup jpaGroup : someJpaGroups) {
            groups.add(groupFrom(jpaGroup, fetchWebServers));
        }
        return groups;
    }

    protected Set<CurrentState<Group, GroupState>> groupStatesFrom(final List<JpaGroup> someJpaGroups) {
        final Set<CurrentState<Group, GroupState>> groupStates = new HashSet<>();
        for (final JpaGroup jpaGroup : someJpaGroups) {
            groupStates.add(groupStateFrom(jpaGroup));
        }
        return groupStates;
    }

    @Override
    public CurrentState<Group, GroupState> updateState(SetStateRequest<Group, GroupState> setStateRequest) {
        return groupStateFrom(groupCrudService.updateGroupStatus(setStateRequest));
    }

    @Override
    public CurrentState<Group, GroupState> getState(Identifier<Group> anId, StateType stateType) {
        return groupStateFrom(groupCrudService.getGroup(anId));
    }

    @Override
    public Set<CurrentState<Group, GroupState>> getAllKnownStates() {
        final List<JpaGroup> groups = groupCrudService.getGroups();
        return groupStatesFrom(groups);
    }

    @Override
    public Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting) {
        groupJvmRelationshipService.populateJvmConfig(uploadJvmTemplateCommands, user, overwriteExisting);
        return groupFrom(groupCrudService.getGroup(aGroupId), false);
    }

    @Override
    public Group updateGroupStatus(SetGroupStateRequest setGroupStateRequest) {
        LOGGER.debug("Persisting new state " + setGroupStateRequest);
        return groupFrom(setGroupStateRequest.getNewState(), groupCrudService.updateGroupStatus(setGroupStateRequest));
    }

    @Override
    public Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateRequests) {
        final JpaGroup group = groupCrudService.getGroup(groupName);
        for (UploadJvmTemplateRequest uploadRequest : uploadJvmTemplateRequests) {
            groupCrudService.uploadGroupJvmTemplate(uploadRequest, group);
        }
        return groupFrom(group, false);
    }

    @Override
    public Group populateGroupWebServerTemplates(String groupName, Map<String, UploadWebServerTemplateRequest> uploadWSTemplateRequests) {
        final JpaGroup group = groupCrudService.getGroup(groupName);
        // check for pre-existing httpd.conf
//        for (String uploadRequestDeployFileName : uploadWSTemplateRequests.keySet()) {
//            if (uploadRequestDeployFileName.equals("httpd.conf"))
//            {
//                for(String resourceTemplateName : getGroupWebServersResourceTemplateNames(groupName)){
//                    if (resourceTemplateName.equals("httpd.conf")){
//                        // TODO log this error
//                        throw new InternalErrorException(AemFaultType.HTTPD_CONF_TEMPLATE_ALREADY_EXISTS, "The group " + groupName + " already has a httpd.conf template for the web servers. Please delete the existing httpd.conf template and try again.");
//                    }
//                }
//            }
//        }

        // upload all of the templates
        for (String uploadRequestDeployFileName : uploadWSTemplateRequests.keySet()) {
            groupCrudService.uploadGroupWebServerTemplate(uploadWSTemplateRequests.get(uploadRequestDeployFileName), group);
        }
        return groupFrom(group, false);
    }

    @Override
    public List<String> getGroupJvmsResourceTemplateNames(String groupName) {
        return groupCrudService.getGroupJvmsResourceTemplateNames(groupName);
    }

    @Override
    public List<String> getGroupWebServersResourceTemplateNames(String groupName) {
        return groupCrudService.getGroupWebServersResourceTemplateNames(groupName);
    }

    @Override
    public List<String> getGroupAppsResourceTemplateNames(String groupName) {
        return groupCrudService.getGroupAppsResourceTemplateNames(groupName);
    }

    @Override
    public String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content) {
        groupCrudService.updateGroupJvmResourceTemplate(groupName, resourceTemplateName, content);
        return groupCrudService.getGroupJvmResourceTemplate(groupName, resourceTemplateName);
    }

    @Override
    public String updateGroupJvmResourceMetaData(String groupName, String resourceName, String metaData) {
        groupCrudService.updateGroupJvmResourceMetaData(groupName, resourceName, metaData);
        return groupCrudService.getGroupJvmResourceTemplateMetaData(groupName, resourceName);
    }

    @Override
    public String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName) {
        return groupCrudService.getGroupJvmResourceTemplate(groupName, resourceTemplateName);
    }

    @Override
    public String getGroupJvmResourceTemplateMetaData(String groupName, String fileName) {
        return groupCrudService.getGroupJvmResourceTemplateMetaData(groupName, fileName);
    }

    @Override
    public String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content) {
        groupCrudService.updateGroupWebServerResourceTemplate(groupName, resourceTemplateName, content);
        return groupCrudService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName);
    }

    @Override
    public String updateGroupWebServerResourceMetaData(String groupName, String resourceName, String metaData) {
        groupCrudService.updateGroupWebServerResourceMetaData(groupName, resourceName, metaData);
        return groupCrudService.getGroupWebServerResourceTemplateMetaData(groupName, resourceName);
    }

    @Override
    public String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName) {
        return groupCrudService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName);
    }

    @Override
    public String getGroupWebServerResourceTemplateMetaData(String groupName, String resourceTemplateName) {
        return groupCrudService.getGroupWebServerResourceTemplateMetaData(groupName, resourceTemplateName);
    }

    @Override
    public ConfigTemplate populateGroupAppTemplate(final String groupName, String appName, final String templateFileName,
                                                   final String metaData, final String templateContent) {
        return groupCrudService.populateGroupAppTemplate(groupName, appName, templateFileName, metaData, templateContent);
    }

    @Override
    public String updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content) {
        groupCrudService.updateGroupAppResourceTemplate(groupName, appName, resourceTemplateName, content);
        return groupCrudService.getGroupAppResourceTemplate(groupName, appName, resourceTemplateName);
    }

    @Override
    public String getGroupAppResourceTemplateMetaData(String groupName, String fileName) {
        return groupCrudService.getGroupAppResourceTemplateMetaData(groupName, fileName);
    }

    @Override
    public String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName) {
        return groupCrudService.getGroupAppResourceTemplate(groupName, appName, resourceTemplateName);
    }

    @Override
    public void updateState(final Identifier<Group> id, final GroupState state) {
        groupCrudService.updateState(id, state);
    }

    @Override
    public boolean checkGroupJvmResourceFileName(final String groupName, final String fileName) {
        return groupCrudService.checkGroupJvmResourceFileName(groupName, fileName);
    }

    @Override
    public boolean checkGroupAppResourceFileName(String groupName, String fileName) {
        return groupCrudService.checkGroupAppResourceFileName(groupName, fileName);
    }

    @Override
    public boolean checkGroupWebServerResourceFileName(String groupName, String fileName) {
        return groupCrudService.checkGroupWebServerResourceFileName(groupName, fileName);
    }

    @Override
    public List<String> getHosts(final String groupName) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_HOSTS_OF_A_GROUP);
        q.setParameter(JpaGroup.QUERY_PARAM_NAME, groupName);
        return q.getResultList();
    }
}
