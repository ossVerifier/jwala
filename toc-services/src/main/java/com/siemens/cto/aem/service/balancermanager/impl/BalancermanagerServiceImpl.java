package com.siemens.cto.aem.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.balancermanager.BalancermanagerService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * w_status_N=1 means drain mode
 * w_status_N=0 means regular mode
 * <p>
 * b=lb-health-check-4.0 means balancer name
 * w=https://usmlvv1cds0049:9111/hct means jvm url (encoded)
 * w=https%3A%2F%2Fusmlvv1cds0049%3A9111%2Fhct
 * <p>
 * w_status_N=1&b=lb-health-check-4.0&w=https%3A%2F%2Fusmlvv1cds0049%3A9111%2Fhct
 */

public class BalancermanagerServiceImpl implements BalancermanagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancermanagerServiceImpl.class);

    private GroupService groupService;
    private WebServerCommandService webServerCommandService;
    private BalancemanagerHttpClient balancemanagerHttpClient;

    @Autowired
    public BalancermanagerServiceImpl(final GroupService groupService,final WebServerCommandService webServerCommandService) {
        this.groupService = groupService;
        this.webServerCommandService = webServerCommandService;
    }

    @Override
    public HttpStatus drainUserGroup(String groupName) {
        System.out.println("groupName: " + groupName);
        Group group = groupService.getGroup(groupName);
        balancemanagerHttpClient = new BalancemanagerHttpClient();
        for (Application application : group.getApplications()) {
            System.out.println("application.getName(): " + application.getName());
            Set<String> appPathSet = getAppPathByGroup(group, application.getName());
            for (WebServer webServer : group.getWebServers()) {
                System.out.println("webServer.getName(): " + webServer.getName());
                Set<String> balanceMemberSet = getBalanceMembers(getHttpdConf(webServer.getId()), application.getName());
                System.out.println("balanceMemberSet.size(): " + balanceMemberSet.size());
                Set<String> drainSet = getMatchJvm(appPathSet, balanceMemberSet);
                System.out.println("drainSet.size(): " + drainSet.size());
                String balancermanagerurl = getBalancerManagerUrl(webServer.getHost());
                for (String worker : drainSet) {
                    Map<String, String> postMap = getPostMap(application.getName(), worker);
                    System.out.println("Drain user for webServer: " + balancermanagerurl + " with worker url " + worker);
                    int returnCode = balancemanagerHttpClient.doHttpClientPost(balancermanagerurl, postMap);
                    System.out.println("returnCode: " + returnCode);
                }
            }
        }
        return HttpStatus.OK;
    }

    public String getBalancerManagerUrl(final String host) {
        return "https://" + host + "/balancer-manager";
    }

    /*
    Build map for post data
    ex:
    w_status_N=1
    b=lb-health-check-4.0
    w=https://usmlvv1cds0049:9101/hct
     */
    public Map<String, String> getPostMap(final String appName, final String worker) {
        Map<String, String> maps = new HashMap<>();
        maps.put("w_status_N", "1");
        maps.put("b", "lb-" + appName);
        maps.put("w", worker);
        return maps;
    }

    public Set<String> getAppPathByGroup(final Group group, final String applicationName) {
        System.out.println("Entering getAppPathByGroup");
        System.out.println("group.getName(): " + group.getName() + " ,applicationName: " + applicationName);
        Set<Application> applications = group.getApplications();
        Set<String> jvmPathSet = new HashSet<>();
        for (Application application : applications) {
            System.out.println("application.getName(): " + application.getName());
            if (application.getName().equalsIgnoreCase(applicationName)) {
                List<Jvm> jvms = application.getJvms();
                System.out.println("jvms.size(): " + jvms.size());
                String jvmPath;
                for (Jvm jvm : jvms) {
                    System.out.println("jvm.getJvmName(): " + jvm.getJvmName() +
                            " jvm.getHostName(): " + jvm.getHostName() +
                            " jvm.getHttpsPort(): " + jvm.getHttpsPort() +
                            " application.getWebAppContext(): " + application.getWebAppContext());
                    if (application.isSecure()) {
                        jvmPath = "https://" + jvm.getHostName() + ":" + jvm.getHttpsPort() + application.getWebAppContext();
                    } else {
                        jvmPath = "http://" + jvm.getHostName() + ":" + jvm.getHttpPort() + application.getWebAppContext();
                    }
                    System.out.println("jvmPath: " + jvmPath);
                    jvmPathSet.add(jvmPath);
                }
            }
        }
        return jvmPathSet;
    }

    /**
     * @param groupJvmSet
     * @param balanceMemberSet
     * @return
     */
    public Set<String> getMatchJvm(final Set<String> groupJvmSet, final Set<String> balanceMemberSet) {
        Set<String> returnSet = new HashSet<>();
        for (String jvm : groupJvmSet) {
            if (balanceMemberSet.contains(jvm)) {
                returnSet.add(jvm);
            }
        }
        return returnSet;
    }

    /*
    Find the proxy balancer setup in httpdConf
    and make sure the BalancerMember is in this httpdConf
    and create the list which is in this httpdConf
     */
    public String getHttpdConf(Identifier<WebServer> webServerId) {
        String httpdConfString = "";
        try {
            CommandOutput commandOutput = webServerCommandService.getHttpdConf(webServerId);
            httpdConfString = commandOutput.toString();
        } catch (CommandFailureException e) {
            LOGGER.warn("Request Failure occurred", e);
        }
        return httpdConfString;
    }

    /**
     * @param httpdConfString Crate a set of jvm from httpdConf BalancerMember
     */
    public Set<String> getBalanceMembers(final String httpdConfString, final String applicationName) {
        InputStream is = new ByteArrayInputStream(httpdConfString.getBytes());
        ApacheConfigParser parser = new ApacheConfigParser();
        Set<String> members = new HashSet<>();
        try {
            ConfigNode configNode = parser.parse(is);
            for (ConfigNode child : configNode.getChildren()) {
                if (child.getName().equals("Proxy")) {
                    System.out.println("Proxy: " + child.getContent());
                    final String balancerName = "balancer://lb-" + applicationName;
                    if (balancerName.equalsIgnoreCase(child.getContent())) {
                        for (ConfigNode memberChild : child.getChildren()) {
                            if (memberChild.getName().equalsIgnoreCase("BalancerMember")) {
                                //System.out.println(memberChild.getContent());
                                String member = memberChild.getContent().toString().substring(0, memberChild.getContent().toString().indexOf(" "));
                                System.out.println(member);
                                members.add(member);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
        LOGGER.info("member.size: " + members.size());
        return members;
    }

    @Override
    public void drainUserWebServer(final String groupName, final String webserverName) {

    }
}
