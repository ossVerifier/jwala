package com.siemens.cto.aem.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
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
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        return balancemanagerHttpClient.doHttpClientPost(managerurl, postMap);
    }

    @Override
    public void drainUserGroup(String groupName) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName);
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            Set<String> appPathSet = getAppPathByGroup(group, application.getName());
            for (WebServer webServer : webServerService.findWebServers(group.getId())) {
                //String httpdConfString = getHttpdConffromResource(webServer.getName());
                //Set<String> balanceMemberSet = getBalanceMembers(httpdConfString, application.getName());
                //Set<String> drainSet = getMatchJvm(appPathSet, balanceMemberSet);
                String balancerManagerurl = getBalancerManagerUrl(webServer.getHost());
                ClientHttpResponse response = getBalancerManagerResponse(balancerManagerurl);
                LOGGER.info(response.toString());
                //doDrain(drainSet, application, balancerManagerurl, webServer);
            }
        }
    }

    @Override
    public void drainUserWebServer(final String groupName, final String webServerName) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServerName: " + webServerName);
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            Set<String> appPathSet = getAppPathByGroup(group, application.getName());
            WebServer webServer = webServerService.getWebServer(webServerName);
            String httpdConfString = getHttpdConffromResource(webServer.getName());
            Set<String> balanceMemberSet = getBalanceMembers(httpdConfString, application.getName());
            Set<String> drainSet = getMatchJvm(appPathSet, balanceMemberSet);
            String balancerManagerurl = getBalancerManagerUrl(webServer.getHost());
            doDrain(drainSet, application, balancerManagerurl, webServer);
        }
    }


    public void doDrain(final Set<String> workers, final Application application, final String balancerManagerurl, final WebServer webServer) {
        LOGGER.info("Entering doDrain");
        for (String worker : workers) {
            Map<String, String> postMap = getPostMap(application.getName(), worker);
            final String message = "Drain user for webServer: " + balancerManagerurl + " with worker url " + worker;
            sendMessage(webServer.getId(), message);
            int returnCode = drainUser(balancerManagerurl, postMap);
            sendMessage(webServer.getId(), Integer.toString(returnCode));
        }
    }

    public void sendMessage(final Identifier<WebServer> id, final String message) {
        LOGGER.info(message);
        messagingService.send(new WebServerHistoryEvent(id, "history", User.getThreadLocalUser().getId(), message));
    }

    public String getBalancerManagerUrl(final String host) {
        return "https://" + host + "/balancer-Manager";
    }

    public Map<String, String> getPostMap(final String appName, final String worker) {
        Map<String, String> maps = new HashMap<>();
        maps.put("w_status_N", "1");
        maps.put("b", "lb-" + appName);
        maps.put("w", worker);
        return maps;
    }

    public Set<String> getAppPathByGroup(final Group group, final String applicationName) {
        LOGGER.info("Entering getAppPathByGroup");
        Set<String> jvmPathSet = new HashSet<>();
        for (Application application : applicationService.findApplications(group.getId())) {
            if (application.getName().equalsIgnoreCase(applicationName)) {
                Set<Jvm> jvms = group.getJvms();
                String jvmPath;
                for (Jvm jvm : jvms) {
                    if (application.isSecure()) {
                        jvmPath = "https://" + jvm.getHostName() + ":" + jvm.getHttpsPort() + application.getWebAppContext();
                    } else {
                        jvmPath = "http://" + jvm.getHostName() + ":" + jvm.getHttpPort() + application.getWebAppContext();
                    }
                    jvmPathSet.add(jvmPath);
                }
            }
        }
        return jvmPathSet;
    }

    public Set<String> getMatchJvm(final Set<String> groupJvmSet, final Set<String> balanceMemberSet) {
        Set<String> returnSet = new HashSet<>();
        for (String jvm : groupJvmSet) {
            if (balanceMemberSet.contains(jvm)) {
                returnSet.add(jvm);
            }
        }
        return returnSet;
    }

    //TODO: Get httpd.conf from database instead of actual file from sever
    public String getHttpdConffromResource(final String webServerName) {
        return webServerService.getResourceTemplate(webServerName, "httpd.conf", true, new ResourceGroup());
    }

    public Set<String> getBalanceMembers(final String httpdConfString, final String applicationName) {
        LOGGER.info("Entering getBalanceMembers, httpdConfString.length(): " + httpdConfString.length() + " , applicationName: " + applicationName);
        InputStream is = new ByteArrayInputStream(httpdConfString.getBytes());
        ApacheConfigParser parser = new ApacheConfigParser();
        Set<String> members = new HashSet<>();
        final String balancerName = "balancer://lb-" + applicationName;
        try {
            ConfigNode configNode = parser.parse(is);
            for (ConfigNode child : configNode.getChildren()) {
                if ((child.getName().equals("Proxy")) && (balancerName.equalsIgnoreCase(child.getContent()))) {
                    for (ConfigNode memberChild : child.getChildren()) {
                        if (memberChild.getName().equalsIgnoreCase("BalancerMember")) {
                            String member = memberChild.getContent().toString().substring(0, memberChild.getContent().toString().indexOf(" "));
                            members.add(member);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.toString());
            throw new ApplicationException("Failed to parsing the httpdConfString ", e);
        }
        LOGGER.info("member.size: " + members.size());
        return members;
    }

    public ClientHttpResponse getBalancerManagerResponse(final String statusUri) {
        try {
            return clientFactoryHelper.requestGet(new URI(statusUri));
        } catch (IOException e) {
            LOGGER.error(e.toString());
            throw new ApplicationException("Failed to get the response for Balancer Manager ", e);
        } catch (URISyntaxException e) {
            LOGGER.error(e.toString());
            throw new ApplicationException("Failed to cannot convert this path to URI ", e);
        }
    }

    @Override
    public void getGroupDrainStatus(String groupName) {
        LOGGER.info("Entering getGroupDrainStatus: " + groupName);
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            Set<String> appPathSet = getAppPathByGroup(group, application.getName());
            for (WebServer webServer : webServerService.findWebServers(group.getId())) {
                String httpdConfString = getHttpdConffromResource(webServer.getName());
                Set<String> balanceMemberSet = getBalanceMembers(httpdConfString, application.getName());
                Set<String> drainSet = getMatchJvm(appPathSet, balanceMemberSet);
                String balancerManagerurl = getBalancerManagerUrl(webServer.getHost());
                //TODO: next
            }
        }
    }

    public boolean getWorkerStatus(final String balancerManagerContent, final String appName, Set<String> workers) {
        boolean drain = false;
        for (String worker : workers) {
            final String matchPattern = "lb-" + appName + "\\&w=" + worker + "\\&nonce=\">.*</a></td><td>.*</td><td></td><td>1</td><td>0</td><td>Init.*Ok </td>";
            Pattern pattern = Pattern.compile(matchPattern);
            Matcher matcher = pattern.matcher(balancerManagerContent);
            while (matcher.find()) {
                drain = isDrain(matcher.group());
            }
            LOGGER.info("worker: " + worker + " drain: " + drain);
        }
        return drain;
    }

    public boolean isDrain(final String status) {
        return (status.indexOf("Init Drn Ok") == -1) ? false : true;
    }

    public Manager getWorkerXml(final String balancerManagerContent, final String appName, Set<String> workers){
        Manager manager = new Manager();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Manager.class);
            Unmarshaller  unmarshal = jaxbContext.createUnmarshaller();
            manager = (Manager) unmarshal.unmarshal(IOUtils.toInputStream(balancerManagerContent));
            LOGGER.info("manager.getBalancers().size(): " + manager.getBalancers().size());
            List<Manager.Balancer> balancers = manager.getBalancers();
            for(Manager.Balancer balancer : balancers) {
                List<Manager.Balancer.Worker> balancer_workers = balancer.getWorkers();
                LOGGER.info("balancer_workers.size(): " + balancer_workers.size());
                for(Manager.Balancer.Worker worker : balancer_workers){
                    LOGGER.info(worker.getName() + " " + worker.getRoute());
                }
            }
            //LOGGER.info(manager);
        } catch (JAXBException e) {
            LOGGER.error(e.toString());
            throw new ApplicationException("Failed to Parsing the Balancer Manager XML ", e);
        }
        return manager;
    }
}
