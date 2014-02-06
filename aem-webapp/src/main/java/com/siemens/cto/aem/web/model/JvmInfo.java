package com.siemens.cto.aem.web.model;

/**
 * Wrapper for JVM related information meant for controller-view serialization
 * hence it's properties are immutable once created.
 */
public class JvmInfo {
    private String name;
    private String host;
    private String httpPort;
    private String availableHeap;
    private String totalHeap;
    private String httpSessionCount;
    private String httpRequestCount;
    private String group;

    private JvmInfo() {
        // Prevent instantiation.
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public String getAvailableHeap() {
        return availableHeap;
    }

    public String getTotalHeap() {
        return totalHeap;
    }

    public String getHttpSessionCount() {
        return httpSessionCount;
    }

    public String getHttpRequestCount() {
        return httpRequestCount;
    }

    public String getGroup() {
        return group;
    }

    /**
     * Builds a {@link com.siemens.cto.aem.web.model.JvmInfo} object.
     */
    public static final class Builder {
        private String name;
        private String host;
        private String httpPort;
        private String availableHeap;
        private String totalHeap;
        private String httpSessionCount;
        private String httpRequestCount;
        private String group;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setHttpPort(String httpPort) {
            this.httpPort = httpPort;
            return this;
        }

        public Builder setAvailableHeap(String availableHeap) {
            this.availableHeap = availableHeap;
            return this;
        }

        public Builder setTotalHeap(String totalHeap) {
            this.totalHeap = totalHeap;
            return this;
        }

        public Builder setHttpSessionCount(String httpSessionCount) {
            this.httpSessionCount = httpSessionCount;
            return this;
        }

        public Builder setHttpRequestCount(String httpRequestCount) {
            this.httpRequestCount = httpRequestCount;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public JvmInfo build() {
            JvmInfo jvmInfo = new JvmInfo();
            jvmInfo.name = this.name;
            jvmInfo.host = this.host;
            jvmInfo.httpPort = this.httpPort;
            jvmInfo.availableHeap = this.availableHeap;
            jvmInfo.totalHeap = this.totalHeap;
            jvmInfo.httpSessionCount = this.httpSessionCount;
            jvmInfo.httpRequestCount = this.httpRequestCount;
            jvmInfo.group = this.group;
            return jvmInfo;
        }

    }

}
