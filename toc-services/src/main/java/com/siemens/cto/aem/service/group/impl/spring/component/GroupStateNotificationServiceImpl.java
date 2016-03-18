package com.siemens.cto.aem.service.group.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.service.exception.GroupStateNotificationServiceException;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link GroupStateNotificationService} implementation.
 *
 * Created by JC043760 on 3/14/2016.
 */
@Service
public class GroupStateNotificationServiceImpl implements GroupStateNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupStateNotificationServiceImpl.class);

    private static final String NULL_STR = "NULL_STR";

    private static final String TOPIC_SERVER_STATES = "/topic/server-states";

    @Autowired
    private JvmCrudService jvmCrudService;

    @Autowired
    private WebServerCrudService webServerCrudService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    @Async
    @SuppressWarnings("unchecked")
    public void retrieveStateAndSendToATopic(final Identifier id, final Class aClass, final String topic) {
        LOGGER.debug("Synchronizing on {} and {}...", id, aClass);
        synchronized ((aClass.getSimpleName() + id.toString()).intern()) {
            LOGGER.debug("Thread locked on {} and {}...!", id, aClass);
            final List<JpaGroup> groups;
            if (Jvm.class.getName().equals(aClass.getName())) {
                final JpaJvm jvm = jvmCrudService.getJvm(id);
                groups = jvm.getGroups();
            } else if (WebServer.class.getName().equals(aClass.getName())) {
                final JpaWebServer webServer = webServerCrudService.getWebServerAndItsGroups(id.getId());
                groups = webServer.getGroups();
            } else {
                final String errMsg = "Invalid class parameter: " + aClass.getName() + "!";
                LOGGER.error(errMsg);
                throw new GroupStateNotificationServiceException(errMsg);
            }

            for (final JpaGroup group: groups) {
                final Long jvmStartedCount = jvmCrudService.getJvmStartedCount(group.getName());
                final Long jvmCount = jvmCrudService.getJvmCount(group.getName());
                final Long webServerStartedCount = webServerCrudService.getStartedWebServerCount(group.getName());
                final Long webServerCount = webServerCrudService.getWebServerCount(group.getName());
                final CurrentState<Group, GroupState> groupState = new CurrentState<>(new Identifier<Group>(group.getId()),
                        GroupState.GRP_UNKNOWN, DateTime.now(), StateType.GROUP, webServerCount, webServerStartedCount,
                        jvmCount, jvmStartedCount);
                simpMessagingTemplate.convertAndSend(TOPIC_SERVER_STATES, groupState);
                LOGGER.debug("Group '{}' state = {}", group.getName(), groupState);
            }
        }
        LOGGER.debug("Thread locked on {} and {} released!", id, aClass);
    }

}
