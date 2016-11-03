package com.cerner.jwala.service.balancermanager.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.common.domain.model.balancermanager.WorkerStatusType;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.message.WebServerHistoryEvent;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.service.balancermanager.impl.xml.data.Manager;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BalancerManagerServiceImpl implements BalancerManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancerManagerServiceImpl.class);

    private GroupService groupService;
    private ApplicationService applicationService;
    private WebServerService webServerService;
    private JvmService jvmService;

    private ClientFactoryHelper clientFactoryHelper;
    private MessagingService messagingService;
    private HistoryService historyService;
    private BalancerManagerHtmlParser balancerManagerHtmlParser;
    private BalancerManagerXmlParser balancerManagerXmlParser;
    private BalancerManagerHttpClient balancerManagerHttpClient;

    private String balancerManagerResponseHtml;
    private String balancerManagerResponseXml;
    private String user;

    public BalancerManagerServiceImpl(final GroupService groupService,
                                      final ApplicationService applicationService,
                                      final WebServerService webServerService,
                                      final JvmService jvmService,
                                      final ClientFactoryHelper clientFactoryHelper,
                                      final MessagingService messagingService,
                                      final HistoryService historyService,
                                      final BalancerManagerHtmlParser balancerManagerHtmlParser,
                                      final BalancerManagerXmlParser balancerManagerXmlParser,
                                      final BalancerManagerHttpClient balancerManagerHttpClient) {
        this.groupService = groupService;
        this.applicationService = applicationService;
        this.webServerService = webServerService;
        this.jvmService = jvmService;
        this.clientFactoryHelper = clientFactoryHelper;
        this.messagingService = messagingService;
        this.historyService = historyService;
        this.balancerManagerHtmlParser = balancerManagerHtmlParser;
        this.balancerManagerXmlParser = balancerManagerXmlParser;
        this.balancerManagerHttpClient = balancerManagerHttpClient;
    }

    @Override
    public BalancerManagerState drainUserGroup(final String groupName, final String webServers, final String user) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServers: " + webServers);
        this.setUser(user);
        String[] webServerArray = getRequireWebServers(webServers);
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        List<WebServer> webServerList;
        if (webServerArray.length == 0) {
            webServerList = webServerService.findWebServers(group.getId());
        } else {
            webServerList = findMatchWebServers(webServerService.findWebServers(group.getId()), webServerArray);
        }
        checkGroupStatus(groupName);
        for (WebServer webServer : webServerList) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, "", true);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);
        return new BalancerManagerState(groupDrainStatusList);
    }

    @Override
    public BalancerManagerState drainUserWebServer(final String groupName, final String webServerName, final String jvmNames, final String user) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServerName: " + webServerName + " jvmNames: " + jvmNames);
        this.setUser(user);
        String[] jvmArray = getRequireJvms(jvmNames);
        checkStatus(webServerService.getWebServer(webServerName));
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        WebServer webServer = webServerService.getWebServer(webServerName);
        if (jvmArray.length == 0) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, "", true);
            webServerDrainStatusList.add(webServerDrainStatus);
        } else {
            for (String jvmName : jvmArray) {
                findJvmIfExists(jvmName);
                BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, jvmName, true);
                webServerDrainStatusList.add(webServerDrainStatus);
            }
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);
        return new BalancerManagerState(groupDrainStatusList);
    }

    public Jvm findJvmIfExists(String jvmName) {
        Jvm jvm;
        try {
            jvm = jvmService.getJvm(jvmName.trim());
        } catch (javax.persistence.NoResultException e) {
            LOGGER.error(e.getMessage(), e);
            String message = "Cannot find " + jvmName + ", please verify if it is valid jvmName";
            throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, message);
        }
        return jvm;
    }

    @Override
    public BalancerManagerState drainUserJvm(final String jvmName, final String user) {
        LOGGER.info("Entering drainUserGroup, jvmName: " + jvmName);
        this.setUser(user);
        Jvm jvm = findJvmIfExists(jvmName);
        Set<Group> groupSet = jvm.getGroups();
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        for (Group group : groupSet) {
            String groupName = group.getName();
            List<WebServer> webServerList = webServerService.findWebServers(group.getId());
            checkGroupStatus(groupName);
            List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
            for (WebServer webServer : webServerList) {
                BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, jvmName, true);
                webServerDrainStatusList.add(webServerDrainStatus);
            }
            BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
            groupDrainStatusList.add(groupDrainStatus);
        }
        return new BalancerManagerState(groupDrainStatusList);
    }

    @Override
    public BalancerManagerState drainUserGroupJvm(final String groupName, final String jvmName, final String user) {
        LOGGER.info("Entering drainUserGroupJvm, groupName: " + groupName + ", jvmName: " + jvmName);
        this.setUser(user);
        checkGroupStatus(groupName);
        Group group = groupService.getGroup(groupName);
        group = groupService.getGroupWithWebServers(group.getId());
        verifyJvmExistInGroup(group, jvmName);
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        for (WebServer webServer : group.getWebServers()) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, jvmName, true);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);
        return new BalancerManagerState(groupDrainStatusList);
    }

    public void verifyJvmExistInGroup(Group group, String jvmName) {
        boolean found = false;
        for (Jvm jvm : group.getJvms()) {
            if (jvm.getJvmName().equalsIgnoreCase(jvmName)) {
                found = true;
            }
        }
        if (!found) {
            String message = "Cannot find " + jvmName + " in group: " + group.getName() + ", please verify if it is valid jvmName";
            throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, message);
        }
    }

    @Override
    public BalancerManagerState getGroupDrainStatus(final String groupName, final String user) {
        LOGGER.info("Entering getGroupDrainStatus: " + groupName);
        this.setUser(user);
        checkGroupStatus(groupName);
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        for (WebServer webServer : webServerService.findWebServers(group.getId())) {
            BalancerManagerState.GroupDrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, "", false);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        BalancerManagerState.GroupDrainStatus groupDrainStatus = new BalancerManagerState.GroupDrainStatus(groupName, webServerDrainStatusList);
        List<BalancerManagerState.GroupDrainStatus> groupDrainStatusList = new ArrayList<>();
        groupDrainStatusList.add(groupDrainStatus);
        return new BalancerManagerState(groupDrainStatusList);
    }

    public void checkGroupStatus(final String groupName) {
        final Group group = groupService.getGroup(groupName);
        List<WebServer> webServerList = webServerService.findWebServers(group.getId());
        for (WebServer webServer : webServerList) {
            if (!webServerService.isStarted(webServer)) {
                final String message = "The target Web Server " + webServer.getName() + " in group " + groupName + " must be STARTED before attempting to drain users";
                LOGGER.error(message);
                throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, message);
            }
        }
    }

    public void checkStatus(WebServer webServer) {
        if (!webServerService.isStarted(webServer)) {
            final String message = "The target Web Server " + webServer.getName() + " must be STARTED before attempting to drain users";
            LOGGER.error(message);
            throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, message);
        }
    }

    public String[] getRequireWebServers(final String webServers) {
        if (webServers.length() != 0) {
            return webServers.split(",");
        } else {
            return new String[0];
        }
    }

    public String[] getRequireJvms(final String jvms) {
        if (jvms.length() != 0) {
            return jvms.split(",");
        } else {
            return new String[0];
        }
    }

    public List<WebServer> findMatchWebServers(final List<WebServer> webServers, final String[] webServerArray) {
        LOGGER.info("Entering findMatchWebServers");
        List<WebServer> webServersMatch = new ArrayList<>();
        List<String> webServerNameMatch = new ArrayList<>();
        Map<Integer, String> webServerNamesIndex = new HashMap<>();
        for (WebServer webServer : webServers) {
            webServerNamesIndex.put(webServers.indexOf(webServer), webServer.getName());
        }
        for (String webServerArrayContentName : webServerArray) {
            if (webServerNamesIndex.containsValue(webServerArrayContentName.trim())) {
                int index = 0;
                for (Map.Entry<Integer, String> entry : webServerNamesIndex.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(webServerArrayContentName.trim())) {
                        index = entry.getKey();
                    }
                }
                webServersMatch.add(webServers.get(index));
                webServerNameMatch.add(webServers.get(index).getName());
            }
        }
        String wrongWebServers = "";
        for (String webServerArrayContentName : webServerArray) {
            if (!webServerNameMatch.contains(webServerArrayContentName.trim())) {
                LOGGER.error("WebServer Name does not exist: " + webServerArrayContentName.trim());
                wrongWebServers += webServerArrayContentName.trim() + ", ";
            }
        }
        if (wrongWebServers.length() != 0) {
            throw new InternalErrorException(AemFaultType.WEBSERVER_NOT_FOUND, wrongWebServers.substring(0, wrongWebServers.length() - 2) + " cannot be found in the group");
        }
        return webServersMatch;
    }

    public BalancerManagerState.GroupDrainStatus.WebServerDrainStatus doDrainAndgetDrainStatus(final WebServer webServer, final String jvmName, final Boolean post) {
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = prepareDrainWork(webServer, jvmName, post);
        return new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus(webServer.getName(), jvmDrainStatusList);
    }

    public List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus> prepareDrainWork(final WebServer webServer, final String jvmName, final Boolean post) {
        LOGGER.info("Entering prepareDrainWork");
        List<BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();
        final String balancerManagerHtmlUrl = balancerManagerHtmlParser.getUrlPath(webServer.getHost());
        balancerManagerResponseHtml = getBalancerManagerResponse(balancerManagerHtmlUrl);
        final Map<String, String> balancers = balancerManagerHtmlParser.findBalancers(balancerManagerResponseHtml);
        for (Map.Entry<String, String> entry : balancers.entrySet()) {
            final String balancerName = entry.getKey();
            final String nonce = entry.getValue();
            final String balancerManagerXmlUrl = balancerManagerXmlParser.getUrlPath(webServer.getHost(), balancerName, nonce);
            balancerManagerResponseXml = getBalancerManagerResponse(balancerManagerXmlUrl);
            Manager manager = balancerManagerXmlParser.getWorkerXml(balancerManagerResponseXml);
            Map<String, String> workers;
            if (jvmName == "") {
                workers = balancerManagerXmlParser.getWorkers(manager, balancerName);
            } else {
                workers = balancerManagerXmlParser.getJvmWorker(manager, balancerName, findJvmUrl(jvmName));
            }
            if (post) {
                doDrain(workers, balancerManagerHtmlUrl, webServer, balancerName, nonce);
            }
            for (String worker : workers.keySet()) {
                String workerUrl = balancerManagerHtmlParser.getWorkerUrlPath(webServer.getHost(), balancerName, nonce, worker);
                String workerHtml = getBalancerManagerResponse(workerUrl);
                Map<String, String> workerStatusMap = balancerManagerHtmlParser.findWorkerStatus(workerHtml);
                BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus jvmDrainStatus = new BalancerManagerState.GroupDrainStatus.WebServerDrainStatus.JvmDrainStatus(worker,
                        balancerManagerXmlParser.findJvmNameByWorker(worker),
                        findApplicationNameByWorker(worker),
                        workerStatusMap.get(WorkerStatusType.IGNORE_ERRORS.name()),
                        workerStatusMap.get(WorkerStatusType.DRAINING_MODE.name()),
                        workerStatusMap.get(WorkerStatusType.DISABLED.name()),
                        workerStatusMap.get(WorkerStatusType.HOT_STANDBY.name()));
                jvmDrainStatusList.add(jvmDrainStatus);
            }
        }
        return jvmDrainStatusList;
    }

    public String findJvmUrl(final String jvmName) {
        String jvmUrl = "";
        if (jvmName == null) return jvmUrl;
        List<Application> applications = applicationService.getApplications();
        for (Application application : applications) {
            boolean isSecure = application.isSecure();
            String webAppContext = application.getWebAppContext();
            Group group = application.getGroup();
            Set<Jvm> jvms = group.getJvms();
            for (Jvm groupJvm : jvms) {
                if (groupJvm.getJvmName().equalsIgnoreCase(jvmName)) {
                    if (isSecure) {
                        jvmUrl = "https://" + groupJvm.getHostName() + ":" + groupJvm.getHttpsPort() + webAppContext;
                    } else {
                        jvmUrl = "http://" + groupJvm.getHostName() + ":" + groupJvm.getHttpPort() + webAppContext;
                    }
                    break;
                }
            }
        }
        LOGGER.info("jvmUrl: " + jvmUrl);
        return jvmUrl;
    }

    public String findApplicationNameByWorker(final String worker) {
        LOGGER.info("Entering findApplicationNameByWorker");
        String appName = "";
        int indexOfLastColon = worker.lastIndexOf(":");
        int firstIndexOfSlashAfterLastColon = worker.substring(indexOfLastColon).indexOf("/");
        int indexOfSlashAfterPort = indexOfLastColon + firstIndexOfSlashAfterLastColon;
        final String context = worker.substring(indexOfSlashAfterPort);
        LOGGER.info("context: " + context);
        List<Application> applications = applicationService.getApplications();
        for (Application application : applications) {
            if (application.getWebAppContext().equalsIgnoreCase(context)) {
                appName = application.getName();
                break;
            }
        }
        return appName;
    }


    public String getBalancerManagerResponse(final String statusUri) {
        LOGGER.info("Entering getBalancerManagerResponse: " + statusUri);
        try {
            return IOUtils.toString(clientFactoryHelper.requestGet(new URI(statusUri)).getBody(), "UTF-8");
        } catch (IOException e) {
            LOGGER.error(e.toString());
            throw new ApplicationException("Failed to get the response for Balancer Manager ", e);
        } catch (URISyntaxException e) {
            LOGGER.error(e.toString());
            throw new ApplicationException("Failed to cannot convert this path to URI ", e);
        }
    }

    public void doDrain(final Map<String, String> workers,
                        final String balancerManagerurl,
                        final WebServer webServer,
                        final String balancerName,
                        final String nonce) {
        LOGGER.info("Entering doDrain");
        for (String workerUrl : workers.keySet()) {
            final String message = "Drain request for " + workerUrl;
            sendMessage(webServer, message);
            try {
                CloseableHttpResponse response = balancerManagerHttpClient.doHttpClientPost(balancerManagerurl, getNvp(workerUrl, balancerName, nonce));
                LOGGER.info("response code: " + response.getStatusLine().getStatusCode());
                response.close();
            } catch (KeyManagementException e) {
                LOGGER.error(e.getMessage(), e);
                throw new ApplicationException(e);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new ApplicationException(e);
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error(e.getMessage(), e);
                throw new ApplicationException(e);
            }
        }
    }

    public void sendMessage(final WebServer webServer, final String message) {
        LOGGER.info(message);
        messagingService.send(new WebServerHistoryEvent(webServer.getId(), "history", getUser(), message));
        historyService.createHistory(webServer.getName(), new ArrayList<>(webServer.getGroups()), message, EventType.USER_ACTION_INFO, getUser());
    }

    public List<NameValuePair> getNvp(final String worker, final String balancerName, final String nonce) {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("w_status_N", "1"));
        nvps.add(new BasicNameValuePair("b", balancerName));
        nvps.add(new BasicNameValuePair("w", worker));
        nvps.add(new BasicNameValuePair("nonce", nonce));
        return nvps;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
