package com.siemens.cto.aem.service.spring.component.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.StateCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.GroupFiniteStateMachine;
import com.siemens.cto.aem.service.state.StateNotificationService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link GrpStateComputationAndNotificationSvc} implementation.
 *
 * Created by JC043760 on 1/5/2016.
 */
@Service("groupStateComputationAndNotificationService")
public class GrpStateComputationAndNotificationSvcImpl implements GrpStateComputationAndNotificationSvc {

    @Autowired
    private JvmCrudService jvmCrudService;

    @Autowired
    private WebServerCrudService webServerCrudService;

    @Autowired
    @Qualifier("jvmStateCrudService")
    private StateCrudService<Jvm, JvmState> jvmStateCrudService;

    @Autowired
    @Qualifier("webServerStateCrudService")
    private StateCrudService<WebServer, WebServerReachableState> webServerStateCrudService;

    @Autowired
    private StateNotificationService stateNotificationService;

    public GrpStateComputationAndNotificationSvcImpl() {}

    @Override
    public synchronized void computeAndNotify(final Identifier id, final OperationalState state) {
        if (state instanceof JvmState) {
            computeGroupStateAndSendNotification(jvmCrudService.getJvm(id), (JvmState) state);
        } else {
            computeGroupStateAndSendNotification(webServerCrudService.getWebServer(id), (WebServerReachableState) state);
        }
    }

    /**
     * Compute group state with JVMs and do notification.
     * @param currentJvm the current {@link Jvm}
     * @param currentJvmState the current {@link JvmState}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void computeGroupStateAndSendNotification(final JpaJvm currentJvm, final JvmState currentJvmState) {
        for (final JpaGroup group: currentJvm.getGroups()) {
            final List<JpaJvm> jvmList = jvmCrudService.findJvmsBelongingTo(new Identifier<Group>(group.getId()));
            GroupState groupState = GroupState.GRP_UNKNOWN;
            for (final JpaJvm jvm: jvmList) {
                JvmState jvmState;
                if (jvm.getId().equals(currentJvm.getId())) {
                    jvmState = (currentJvmState == null ? JvmState.JVM_UNKNOWN : currentJvmState);
                } else {
                    jvmState = JvmState.valueOf(jvmStateCrudService.getState(new Identifier<Jvm>(jvm.getId())).getState());
                }
                groupState = GroupFiniteStateMachine.getInstance().computeGroupState(groupState, jvmState);
            }

            final List<WebServer> webServerList = webServerCrudService.
                    findWebServersBelongingTo(new Identifier<Group>(group.getId()));
            for (final WebServer webServer: webServerList) {
                groupState = GroupFiniteStateMachine.getInstance().computeGroupState(groupState,
                        WebServerReachableState.valueOf(webServerStateCrudService.getState(webServer.getId()).getState()));
            }

            stateNotificationService.notifyStateUpdated(new CurrentState<>(new Identifier<>(group.getId()), groupState,
                    DateTime.now(), StateType.GROUP));
        }
    }

    /**
     * Compute group state with web servers and do notification.
     * @param currentWebServer the current {@link WebServer}
     * @param currentWebServerState the current {@link WebServerReachableState}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void computeGroupStateAndSendNotification(final WebServer currentWebServer,
                                                      final WebServerReachableState currentWebServerState) {
        for (final Group group: currentWebServer.getGroups()) {
            final List<WebServer> webServerList = webServerCrudService.findWebServersBelongingTo(group.getId()) ;
            GroupState groupState = GroupState.GRP_UNKNOWN;
            for (final WebServer webServer : webServerList) {
                WebServerReachableState webServerState;
                if (webServer.getId().equals(currentWebServer.getId())) {
                    webServerState = (currentWebServerState == null ? WebServerReachableState.WS_UNKNOWN :
                            currentWebServerState);
                } else {
                    webServerState = WebServerReachableState.valueOf(webServerStateCrudService.getState(webServer.getId()).getState());
                }
                groupState = GroupFiniteStateMachine.getInstance().computeGroupState(groupState, webServerState);
            }

            final List<JpaJvm> jvmList = jvmCrudService.findJvmsBelongingTo(group.getId());
            for (final JpaJvm jvm: jvmList) {
                groupState = GroupFiniteStateMachine.getInstance().computeGroupState(groupState,
                        JvmState.valueOf(jvmStateCrudService.getState(new Identifier<Jvm>(jvm.getId())).getState()));
            }

            stateNotificationService.notifyStateUpdated(new CurrentState<>(group.getId(), groupState,
                    DateTime.now(), StateType.GROUP));
        }
    }

}
