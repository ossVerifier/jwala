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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        List<BalancerManagerState.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        List<WebServer> webServerList;
        if (webServerArray.length == 0) {
            webServerList = webServerService.findWebServers(group.getId());
        } else {
            webServerList = findMatchWebServers(webServerService.findWebServers(group.getId()), webServerArray);
        }
        checkGroupStatus(groupName);
        for (WebServer webServer : webServerList) {
            BalancerManagerState.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, true);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        return new BalancerManagerState(groupName, webServerDrainStatusList);
    }

    @Override
    public BalancerManagerState drainUserWebServer(final String groupName, final String webServerName, final String user) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServerName: " + webServerName);
        this.setUser(user);
        checkStatus(webServerService.getWebServer(webServerName));
        List<BalancerManagerState.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        WebServer webServer = webServerService.getWebServer(webServerName);
        BalancerManagerState.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, true);
        webServerDrainStatusList.add(webServerDrainStatus);
        return new BalancerManagerState(groupName, webServerDrainStatusList);
    }

    @Override
    public BalancerManagerState getGroupDrainStatus(final String groupName, final String user) {
        LOGGER.info("Entering getGroupDrainStatus: " + groupName);
        this.setUser(user);
        checkGroupStatus(groupName);
        List<BalancerManagerState.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        for (WebServer webServer : webServerService.findWebServers(group.getId())) {
            BalancerManagerState.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, false);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        return new BalancerManagerState(groupName, webServerDrainStatusList);
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

    public BalancerManagerState.WebServerDrainStatus doDrainAndgetDrainStatus(final WebServer webServer, final Boolean post) {
        List<BalancerManagerState.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = prepareDrainWork(webServer, post);
        return new BalancerManagerState.WebServerDrainStatus(webServer.getName(), jvmDrainStatusList);
    }

    //It seems like no mater what balancer name and nonce I pass, it always return the whole xml format for webServer level
    //In this case, jwala only needs to do it one time instead of go through all balancer name (multiple times) to find out all workers
    //but it still need to pass the balancer name and nonce in order to get xml file
    //We believe it is the issue for apache-tomcat httpd balancer manager
    public List<BalancerManagerState.WebServerDrainStatus.JvmDrainStatus> prepareDrainWork(final WebServer webServer, final Boolean post) {
        LOGGER.info("Entering prepareDrainWork");
        List<BalancerManagerState.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();
        final String balancerManagerHtmlUrl = balancerManagerHtmlParser.getUrlPath(webServer.getHost());
        balancerManagerResponseHtml = getBalancerManagerResponse(balancerManagerHtmlUrl);
        final Map<String, String> balancers = balancerManagerHtmlParser.findBalancers(balancerManagerResponseHtml);
        for (Map.Entry<String, String> entry : balancers.entrySet()) {
            final String balancerName = entry.getKey();
            final String nonce = entry.getValue();
            final String balancerManagerXmlUrl = balancerManagerXmlParser.getUrlPath(webServer.getHost(), balancerName, nonce);
            balancerManagerResponseXml = getBalancerManagerResponse(balancerManagerXmlUrl);
            Manager manager = balancerManagerXmlParser.getWorkerXml(balancerManagerResponseXml);
            Map<String, String> workers = balancerManagerXmlParser.getWorkers(manager, balancerName);
            if (post) {
                doDrain(workers, balancerManagerHtmlUrl, webServer, balancerName, nonce);
            }
            for (String worker : workers.keySet()) {
                String workerUrl = balancerManagerHtmlParser.getWorkerUrlPath(webServer.getHost(), balancerName, nonce, worker);
                String workerHtml = getBalancerManagerResponse(workerUrl);
                Map<String, String> workerStatusMap = balancerManagerHtmlParser.findWorkerStatus(workerHtml);
                BalancerManagerState.WebServerDrainStatus.JvmDrainStatus jvmDrainStatus = new BalancerManagerState.WebServerDrainStatus.JvmDrainStatus(worker,
                        findJvmNameByWorker(worker),
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

    public String findJvmNameByWorker(final String worker) {
        LOGGER.info("Entering findJvmNameByWorker");
        List<Jvm> jvms = jvmService.getJvms();
        String jvmName = "";
        for (Jvm jvm : jvms) {
            String jvmUrl;
            if (worker.indexOf("https") != -1) {
                jvmUrl = "https://" + jvm.getHostName() + ":" + jvm.getHttpsPort();
            } else if (worker.indexOf("http") != -1) {
                jvmUrl = "http://" + jvm.getHostName() + ":" + jvm.getHttpPort();
            } else if (worker.indexOf("ajp") != -1) {
                jvmUrl = "ajp://" + jvm.getHostName() + ":" + jvm.getAjpPort();
            } else {
                return "";
            }
            if (worker.toLowerCase().indexOf(jvmUrl.toLowerCase()) != -1) {
                jvmName = jvm.getJvmName();
                break;
            }
        }
        System.out.println(jvmName);
        return jvmName;
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
            final String message = "Set Drain mode for JVM " + workerUrl;
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
        historyService.createHistory(webServer.getName(), new ArrayList<>(webServer.getGroups()), message, EventType.USER_ACTION, getUser());
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
