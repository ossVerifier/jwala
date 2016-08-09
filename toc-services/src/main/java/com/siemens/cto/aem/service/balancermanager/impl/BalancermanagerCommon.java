package com.siemens.cto.aem.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.balancermanager.DrainStatus;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.message.WebServerHistoryEvent;
import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.balancermanager.impl.xml.data.Manager;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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


public abstract class BalancermanagerCommon {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancermanagerCommon.class);

    private BalancemanagerHttpClient balancemanagerHttpClient;

    private String nonce = "";
    private String balancerName = "";

    private final ClientFactoryHelper clientFactoryHelper;
    private MessagingService messagingService;
    private HistoryService historyService;

    private String balancerManagerResponseHtml;
    private String balancerManagerResponseXml;

    public BalancermanagerCommon(final ClientFactoryHelper clientFactoryHelper,
                                 final MessagingService messagingService,
                                 final HistoryService historyService,
                                 final BalancemanagerHttpClient balancemanagerHttpClient) {
        this.clientFactoryHelper = clientFactoryHelper;
        this.messagingService = messagingService;
        this.historyService = historyService;
        this.balancemanagerHttpClient = balancemanagerHttpClient;
    }

    public DrainStatus.WebServerDrainStatus doDrainAndgetDrainStatus(final WebServer webServer, final Application application, final Boolean post) {
        List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = prepareDrainWork(webServer, application, post);
        return new DrainStatus.WebServerDrainStatus(webServer.getName(), jvmDrainStatusList);
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
        for (String webServerArrayContentName : webServerArray) {
            if (!webServerNameMatch.contains(webServerArrayContentName.trim())) {
                LOGGER.warn("WebServer Name does not exist: " + webServerArrayContentName.trim());
            }
        }
        return webServersMatch;
    }

    public String[] getRequireWebServers(final String webServers) {
        if (webServers.length() != 0) {
            return webServers.split(",");
        } else {
            return new String[0];
        }
    }

    public List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> prepareDrainWork(final WebServer webServer, final Application application, final Boolean post) {
        LOGGER.info("Entering prepareDrainWork");
        final String balancerManagerUrlHtml = getBalancerManagerUrlPath(webServer.getHost(), false);
        balancerManagerResponseHtml = getBalancerManagerResponse(balancerManagerUrlHtml);
        findBalancerName(balancerManagerResponseHtml, application.getName());
        findNonce(balancerManagerResponseHtml);
        final String balancerManagerUrlXml = getBalancerManagerUrlPath(webServer.getHost(), true);
        balancerManagerResponseXml = getBalancerManagerResponse(balancerManagerUrlXml);
        Manager manager = getWorkerXml(balancerManagerResponseXml);
        Map<String, String> workers = getWorkers(manager);
        if (post) {
            doDrain(workers, balancerManagerUrlHtml, webServer);
            balancerManagerResponseHtml = getBalancerManagerResponse(balancerManagerUrlHtml);
        }
        return getJvmsWorkerStatus(balancerManagerResponseHtml, application.getName(), workers);
    }

    public List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> getJvmsWorkerStatus(final String balancerManagerContent, final String appName, Map<String, String> workers) {
        List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();
        DrainStatus.WebServerDrainStatus.JvmDrainStatus jvmDrainStatus;
        for (String workerUrl : workers.keySet()) {
            final String route = workers.get(workerUrl);
            final String matchPattern = getBalancerName() + "\\&w=" + workerUrl + "\\&nonce=.*\">.*</a></td><td>.*</td><td></td><td>1</td><td>0</td><td>Init.*Ok </td>";
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

    public void doDrain(final Map<String, String> workers, final String balancerManagerurl, final WebServer webServer) {
        LOGGER.info("Entering doDrain");
        for (String workerUrl : workers.keySet()) {
            final String message = "Set Drain mode for JVM " + workerUrl;
            sendMessage(webServer, message);
            try {
                CloseableHttpResponse response = balancemanagerHttpClient.doHttpClientPost(balancerManagerurl, getNVP(workerUrl));
                LOGGER.info("response code: " + response.getStatusLine().getStatusCode());
                response.close();
            } catch (KeyManagementException e) {
                LOGGER.error(e.toString());
                throw new ApplicationException(e);
            } catch (IOException e) {
                LOGGER.error(e.toString());
                throw new ApplicationException(e);
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error(e.toString());
                throw new ApplicationException(e);
            }
        }
    }

    public void sendMessage(final WebServer webServer, final String message) {
        LOGGER.info(message);
        User user = User.getThreadLocalUser();
        messagingService.send(new WebServerHistoryEvent(webServer.getId(), "history", user.getId(), message));
        historyService.createHistory(webServer.getName(), new ArrayList<>(webServer.getGroups()), message, EventType.USER_ACTION, user.getId());
    }

    public String getBalancerManagerUrlPath(final String host, boolean isXml) {
        final String url = "https://" + host + "/balancer-manager";
        if (isXml) {
            return url + "?b=" + getBalancerName() + "&xml=1&nonce=" + getNonce();
        } else {
            return url;
        }
    }

    public List<NameValuePair> getNVP(final String worker) {
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("w_status_N", "1"));
        nvps.add(new BasicNameValuePair("b", getBalancerName()));
        nvps.add(new BasicNameValuePair("w", worker));
        nvps.add(new BasicNameValuePair("nonce", getNonce()));
        return nvps;
    }

    public Map<String, String> getWorkers(final Manager manager) {
        LOGGER.info("Entering getWorkers for application: ");
        Map<String, String> workers = new HashMap<>();
        for (Manager.Balancer balancers : manager.getBalancers()) {
            if (balancers.getName().equalsIgnoreCase("balancer://" + getBalancerName())) {
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
            List<Manager.Balancer> balancers = manager.getBalancers();
            for (Manager.Balancer balancer : balancers) {
                LOGGER.info(balancer.getName());
                List<Manager.Balancer.Worker> balancer_workers = balancer.getWorkers();
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

    public void findNonce(final String content) {
        LOGGER.info("Entering findNonce: ");
        final String foundStringPrefix = "<h3>LoadBalancer Status for <a href=\"/balancer-manager\\?b=";
        final String foundStringPost = "\\&nonce=";
        final String foundString = "nonce=";
        final String matchPattern = foundStringPrefix + getBalancerName() + foundStringPost + ".*";
        Pattern pattern = Pattern.compile(matchPattern);
        Matcher matcher = pattern.matcher(content);
        String matchString;
        while (matcher.find()) {
            matchString = matcher.group();
            setNonce(matchString.substring(matchString.indexOf(foundString) + foundString.length(), matchString.indexOf("\">balancer:")));
        }
        LOGGER.info("nonce: " + getNonce());
    }

    public void findBalancerName(final String content, final String appName) {
        LOGGER.info("Entering findBalancerName appName: " + appName.toLowerCase());
        final String foundStringPrefix = "<h3>LoadBalancer Status for <a href=\"/balancer-manager\\?b=";
        final String foundStringPost = "\\&nonce=";
        final String matchPattern = foundStringPrefix + ".*" + appName.toLowerCase() + foundStringPost + ".*";
        Pattern pattern = Pattern.compile(matchPattern);
        Matcher matcher = pattern.matcher(content);
        String matchString;
        while (matcher.find()) {
            matchString = matcher.group();
            LOGGER.debug(matchString);
            setBalancerName(matchString.substring(matchString.indexOf(foundStringPrefix.replace("\\", "")) + foundStringPrefix.replace("\\", "").length(),
                    matchString.indexOf(foundStringPost.replace("\\", ""))));
        }
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getBalancerName() {
        return this.balancerName;
    }

    public void setBalancerName(String balancerName) {
        this.balancerName = balancerName;
    }
}
