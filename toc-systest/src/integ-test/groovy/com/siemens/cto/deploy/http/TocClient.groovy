package com.siemens.cto.deploy.http

/**
 * Created by z003e5zv on 4/8/2015.
 */
class TocClient implements TocClientLegacy {

    private final ClientConnectors connectors;

    private Integer cachedLiveGroupId = null;

    public TocClient(String host, String port, String username, String password) {
        this("http",host,port,username,password);
    }

    public TocClient(String protocol, String host, String port, String username, String password) {
        this.connectors = new ClientConnectors(new TocHttpClient(host, port, username, password));
        println "Created new TocClient : protocol=$protocol, host=$host, port=$port, username=$username, password=$password"
    }

    public void login() {
      this.connectors.getClientForGroup(); // side-effect is that a login occurs.
    }

    public TocClientForGroup getV1GroupClient() {
        return this.connectors.getClientForGroup();
    }
    public TocClientForWebApp getV1WebAppClient() {
        return this.connectors.getClientForWebApp();
    }
    public TocClientForWebServers getV1WebServerClient() {
        return this.connectors.getClientForWebservers();
    }
    public TocClientForJvm getV1JvmClient() {
        return this.connectors.getClientForJvm();
    }
    private synchronized int getCachedGroupId() {
        return this.cachedLiveGroupId;
    }

    @Override
    void deleteGroup(int groupId) {
        if (this.cachedLiveGroupId == groupId) {
            this.cachedLiveGroupId = null;
        }
        this.connectors.getClientForGroup().deleteGroup(groupId);
    }

    @Override
    int getOrCreateGroup(String groupName) {
        int returnedGroupId = this.connectors.getClientForGroup().getOrCreateGroup(groupName);
        this.cachedLiveGroupId = returnedGroupId;
        return returnedGroupId;
    }

    @Override
    int createGroup(String groupName) {
        int groupId = this.connectors.getClientForGroup().createGroup(groupName);
        this.cachedLiveGroupId = groupId;
        return groupId;
    }

    @Override
    String getGroups(String id) {
        return this.connectors.getClientForGroup().getGroups(id);
    }

    @Override
    String getGroupsByName(String groupName) {
      return this.connectors.getClientForGroup().getGroupsByName(groupName);
    }

    @Override
    Map<String, Integer> getJvmIdsForGroupAndServer(String groupName, String hostName) {
        return this.connectors.getClientForGroup().getJvmIdsForGroupAndServer(groupName, hostName);
    }

    @Override
    int addJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties) {
        return this.connectors.getClientForJvm().addJvm(jvmName, hostName, httpsPort, httpsPort, redirectPort,shutdownPort, ajpPort, statusPath, systemProperties, this.getCachedGroupId());
    }

    @Override
    int getOrCreateJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties) {
        return this.connectors.getClientForJvm().getOrCreateJvm(jvmName, hostName, httpPort, httpsPort, redirectPort, shutdownPort, ajpPort, statusPath, systemProperties, this.getCachedGroupId());
    }

    @Override
    void deleteJvm(int jvmId) {
        this.connectors.getClientForJvm().deleteJvm(jvmId);
    }

    @Override
    int addWebApp(String contextPath, String appName) {
        return this.connectors.getClientForWebApp().addWebApp(contextPath, appName, getCachedGroupId());
    }

    @Override
    int getOrCreateWebApp(String contextPath, String appName) {
        return this.connectors.getClientForWebApp().getOrCreateWebApp(contextPath, appName, getCachedGroupId());
    }

    @Override
    void deleteApp(int appId) {
        this.connectors.getClientForWebApp().deleteApp(appId);
    }

    @Override
    int addWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot) {
        return this.connectors.getClientForWebservers().addWebServer(webServerName, hostName, httpPort, httpsPort, statusPath, configPath, svrRoot, docRoot, this.getCachedGroupId());
    }

    @Override
    int getOrCreateWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot) {
        return this.connectors.getClientForWebservers().getOrCreateWebServer(webServerName, hostName, httpPort, httpsPort, statusPath, configPath, svrRoot, docRoot, this.getCachedGroupId());
    }

    @Override
    void deleteWebServer(int webServerId) {
        this.connectors.getClientForWebservers().deleteWebServer(webServerId);
    }

    @Override
    String getWebServers(String groupId) {
        return this.connectors.getClientForWebservers().getWebServers(groupId);
    }

    @Override
    String getWebServerConfig(String name) {
        return this.connectors.getClientForWebservers().getWebServerConfig(name);
    }

    @Override
    public void deployJvmInstance(String jvmName) {
        this.connectors.getClientForJvm().deployJvmInstance(jvmName);
    }


    private interface TocClientLegacy {
        // Groups
        void deleteGroup(int groupId);
        int getOrCreateGroup(String groupName);
        int createGroup(String groupName);
        String getGroups(String id);
        String getGroupsByName(String groupName);
        // JVM
        int addJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties);
        int getOrCreateJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties);
        Map<String,Integer> getJvmIdsForGroupAndServer(String groupName, String hostName);
        void deleteJvm(int jvmId);
        void deployJvmInstance(String jvmName);
        // WebApp
        int addWebApp(String contextPath, String appName);
        int getOrCreateWebApp(String contextPath, String appName);
        void deleteApp(int appId);
        // Webserver
        int addWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot);
        int getOrCreateWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot);
        void deleteWebServer(int webServerId);
        String getWebServers(String groupId);
        String getWebServerConfig(String name);
    }

    private static class ClientConnectors {

        private TocHttpClient tocHttpClient;

        private TocClientForGroup tocClientForGroup = null;
        private TocClientForJvm tocClientForJvm = null;
        private TocClientForWebApp tocClientForWebApp = null;
        private TocClientForWebServers tocClientForWebServers = null;

        protected ClientConnectors(TocHttpClient tocHttpClient) {
            this.tocHttpClient = tocHttpClient;
        }

        public TocClientForWebApp getClientForWebApp() {
            if (this.tocClientForWebApp == null) {
                this.tocClientForWebApp = new TocClientForWebApp(this.tocHttpClient);
            }
            return this.tocClientForWebApp;
        }
        public TocClientForJvm getClientForJvm() {
            if (this.tocClientForJvm == null) {
                this.tocClientForJvm = new TocClientForJvm(this.tocHttpClient);
            }
            return this.tocClientForJvm;
        }
        public TocClientForGroup getClientForGroup() {
            if (this.tocClientForGroup == null) {
                this.tocClientForGroup = new TocClientForGroup(this.tocHttpClient);
            }
            return this.tocClientForGroup;
        }
        public TocClientForWebServers getClientForWebservers() {
            if (this.tocClientForWebServers == null) {
                this.tocClientForWebServers = new TocClientForWebServers(this.tocHttpClient);
            }
            return this.tocClientForWebServers;
        }
    }
}
