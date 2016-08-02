package com.siemens.cto.aem.common.domain.model.balancermanager;

import java.util.ArrayList;
import java.util.List;

public class DrainStatus {

    private String groupName;
    private List<WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();

    public DrainStatus() {
    }

    public DrainStatus(String groupName, List<WebServerDrainStatus> webServerDrainStatusList) {
        this.groupName = groupName;
        this.webServerDrainStatusList = webServerDrainStatusList;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<WebServerDrainStatus> getWebServerDrainStatusList() {
        return webServerDrainStatusList;
    }

    public void setWebServerDrainStatusList(List<WebServerDrainStatus> webServerDrainStatusList) {
        this.webServerDrainStatusList = webServerDrainStatusList;
    }

    @Override
    public String toString() {
        return "DrainStatus{" +
                "groupName='" + groupName + '\'' +
                ", webServerDrainStatusList=" + webServerDrainStatusList +
                '}';
    }

    public static class WebServerDrainStatus {

        public WebServerDrainStatus(String webServerName, List<JvmDrainStatus> jvmDrainStatusList) {
            this.webServerName = webServerName;
            this.jvmDrainStatusList = jvmDrainStatusList;
        }

        private String webServerName;
        private List<JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();

        public String getWebServerName() {
            return webServerName;
        }

        public void setWebServerName(String webServerName) {
            this.webServerName = webServerName;
        }

        public List<JvmDrainStatus> getJvmDrainStatusList() {
            return jvmDrainStatusList;
        }

        public void setJvmDrainStatusList(List<JvmDrainStatus> jvmDrainStatusList) {
            this.jvmDrainStatusList = jvmDrainStatusList;
        }

        @Override
        public String toString() {
            return "WebServerDrainStatus{" +
                    "webServerName='" + webServerName + '\'' +
                    ", jvmDrainStatusList=" + jvmDrainStatusList +
                    '}';
        }

        public static class JvmDrainStatus {
            private String jvmName;
            private String drainStatus;
            private String appName;

            public JvmDrainStatus(String jvmName, String drainStatus, String appName) {
                this.jvmName = jvmName;
                this.drainStatus = drainStatus;
                this.appName = appName;
            }

            public String getJvmName() {
                return jvmName;
            }

            public void setJvmName(String jvmName) {
                this.jvmName = jvmName;
            }

            public String getDrainStatus() {
                return drainStatus;
            }

            public void setDrainStatus(String drainStatus) {
                this.drainStatus = drainStatus;
            }

            public String getAppName() {
                return appName;
            }

            public void setAppName(String appName) {
                this.appName = appName;
            }

            @Override
            public String toString() {
                return "JvmDrainStatus{" +
                        "jvmName='" + jvmName + '\'' +
                        ", drainStatus='" + drainStatus + '\'' +
                        ", appName='" + appName + '\'' +
                        '}';
            }
        }
    }
}
