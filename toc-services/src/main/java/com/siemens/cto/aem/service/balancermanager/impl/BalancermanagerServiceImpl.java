package com.siemens.cto.aem.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.balancermanager.DrainStatus;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.message.WebServerHistoryEvent;
import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.balancermanager.BalancermanagerService;
import com.siemens.cto.aem.service.balancermanager.impl.xml.data.Manager;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalancermanagerServiceImpl implements BalancermanagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancermanagerServiceImpl.class);

    private GroupService groupService;
    private ApplicationService applicationService;
    private WebServerService webServerService;
    private BalancemanagerHttpClient balancemanagerHttpClient = new BalancemanagerHttpClient();
    private MessagingService messagingService;
    private ClientFactoryHelper clientFactoryHelper;

    private final String balancer_prefix = "lb-";
    private String nonce = "";

    public BalancermanagerServiceImpl(final GroupService groupService,
                                      final ApplicationService applicationService,
                                      final WebServerService webServerService,
                                      final MessagingService messagingService,
                                      final ClientFactoryHelper clientFactoryHelper) {
        this.groupService = groupService;
        this.applicationService = applicationService;
        this.webServerService = webServerService;
        this.messagingService = messagingService;
        this.clientFactoryHelper = clientFactoryHelper;
    }

    public int drainUser(final String managerurl, final Map<String, String> postMap) {
        LOGGER.info("Entering drainUser: " + managerurl);
        return balancemanagerHttpClient.doHttpClientPost(managerurl, postMap);
    }

    @Override
    public void drainUserGroup(String groupName) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName);
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            for (WebServer webServer : webServerService.findWebServers(group.getId())) {
                prepareDrainWork(webServer, application, true);
            }
        }
    }

    @Override
    public void drainUserWebServer(final String groupName, final String webServerName) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServerName: " + webServerName);
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            WebServer webServer = webServerService.getWebServer(webServerName);
            prepareDrainWork(webServer, application, true);
        }
    }

    @Override
    public DrainStatus getGroupDrainStatus(String groupName) {
        LOGGER.info("Entering getGroupDrainStatus: " + groupName);
        List<DrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            for (WebServer webServer : webServerService.findWebServers(group.getId())) {
                List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = prepareDrainWork(webServer, application, false);
                DrainStatus.WebServerDrainStatus webServerDrainStatus = new DrainStatus.WebServerDrainStatus(webServer.getName(), jvmDrainStatusList);
                webServerDrainStatusList.add(webServerDrainStatus);
             }
        }
        return new DrainStatus(groupName, webServerDrainStatusList);
    }

    public List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> prepareDrainWork(final WebServer webServer, final Application application, final Boolean post) {
        String balancerManagerHtmlurl = getBalancerManagerUrlPath(webServer.getHost(), application.getName(), false);
        final String responseStringHtml = getBalancerManagerResponse(balancerManagerHtmlurl);
        findNonce(responseStringHtml, application.getName());
        String balancerManagerXmlurl = getBalancerManagerUrlPath(webServer.getHost(), application.getName(), true);
        final String responseStringXml = getBalancerManagerResponse(balancerManagerXmlurl);
        Manager manager = getWorkerXml(responseStringXml);
        Map<String, String> workers = getWorkers(manager, application.getName());
        if (post){
            doDrain(workers, application, balancerManagerHtmlurl, webServer);
            return null;
        } else {
            return getWorkerStatus(responseStringHtml, application.getName(), workers);
        }
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

    public void doDrain(final Map<String, String> workers, final Application application, final String balancerManagerurl, final WebServer webServer) {
        LOGGER.info("Entering doDrain");
        for (String workerUrl : workers.keySet()) {
            Map<String, String> postMap = getPostMap(application.getName(), workerUrl);
            final String message = "Drain user for webServer: " + balancerManagerurl + " with worker url " + workerUrl;
            sendMessage(webServer.getId(), message);
            int returnCode = drainUser(balancerManagerurl, postMap);
            sendMessage(webServer.getId(), Integer.toString(returnCode));
        }
    }

    public void sendMessage(final Identifier<WebServer> id, final String message) {
        LOGGER.info(message);
        messagingService.send(new WebServerHistoryEvent(id, "history", User.getThreadLocalUser().getId(), message));
    }

    public String getBalancerManagerUrlPath(final String host, final String appName, boolean isXml) {
        final String url = "https://" + host + "/balancer-manager";
        if (isXml) {
            return url + "?b=" + balancer_prefix + appName + "&xml=1&nonce=" + getNonce();
        } else {
            return url;
        }
    }

    public Map<String, String> getPostMap(final String appName, final String worker) {
        Map<String, String> maps = new HashMap<>();
        maps.put("w_status_N", "1");
        maps.put("b", balancer_prefix + appName);
        maps.put("w", worker);
        maps.put("nonce", getNonce());
        return maps;
    }

    public List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> getWorkerStatus(final String balancerManagerContent, final String appName, Map<String, String> workers) {
        List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();
        DrainStatus.WebServerDrainStatus.JvmDrainStatus jvmDrainStatus;
        for (String workerUrl : workers.keySet()) {
            final String route = workers.get(workerUrl);
            final String matchPattern = balancer_prefix + appName.toLowerCase() + "\\&w=" + workerUrl + "\\&nonce=.*\">.*</a></td><td>.*</td><td></td><td>1</td><td>0</td><td>Init.*Ok </td>";
            Pattern pattern = Pattern.compile(matchPattern);
            Matcher matcher = pattern.matcher(balancerManagerContent);
            while (matcher.find()) {
                if (isDrain(matcher.group())) {
                    jvmDrainStatus = new DrainStatus.WebServerDrainStatus.JvmDrainStatus(route, "drain", appName);
                } else {
                    jvmDrainStatus = new DrainStatus.WebServerDrainStatus.JvmDrainStatus(route, "active", appName);
                }
                jvmDrainStatusList.add(jvmDrainStatus);
            }
        }
        return jvmDrainStatusList;
    }

    public boolean isDrain(final String status) {
        return (status.indexOf("Init Drn Ok") == -1) ? false : true;
    }

    public Map<String, String> getWorkers(final Manager manager, final String appName) {
        Map<String, String> workers = new HashMap<>();
        for (Manager.Balancer balancers : manager.getBalancers()) {
            if (balancers.getName().equalsIgnoreCase("balancer://" + balancer_prefix + appName)) {
                for (Manager.Balancer.Worker worker : balancers.getWorkers()) {
                    workers.put(worker.getName(), worker.getRoute());
                }
            }
        }
        return workers;
    }

    public Manager getWorkerXml(final String balancerManagerContent) {
        Manager manager;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Manager.class);
            Unmarshaller unmarshal = jaxbContext.createUnmarshaller();
            manager = (Manager) unmarshal.unmarshal(IOUtils.toInputStream(balancerManagerContent));
            LOGGER.info("manager.getBalancers().size(): " + manager.getBalancers().size());
            List<Manager.Balancer> balancers = manager.getBalancers();
            for (Manager.Balancer balancer : balancers) {
                LOGGER.info(balancer.getName());
                List<Manager.Balancer.Worker> balancer_workers = balancer.getWorkers();
                LOGGER.info("balancer_workers.size(): " + balancer_workers.size());
                for (Manager.Balancer.Worker worker : balancer_workers) {
                    LOGGER.info(worker.getName() + " " + worker.getRoute());
                }
            }
        } catch (JAXBException e) {
            LOGGER.error(e.toString());
            throw new ApplicationException("Failed to Parsing the Balancer Manager XML ", e);
        }
        return manager;
    }

    public void findNonce(final String content, final String appName) {
        LOGGER.info("content: " + content + " appName: " + appName);
        final String matchPattern = "<h3>LoadBalancer Status for <a href=\"/balancer-manager\\?b=" + balancer_prefix + appName.toLowerCase() + "\\&nonce=.*";
        Pattern pattern = Pattern.compile(matchPattern);
        Matcher matcher = pattern.matcher(content);
        String matchString;
        while (matcher.find()) {
            LOGGER.info(matcher.group());
            matchString = matcher.group();
            setNonce(matchString.substring(matchString.indexOf("nonce=") + 6, matchString.indexOf("\">balancer:")));
        }
        LOGGER.info("nonce: " + getNonce());
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
