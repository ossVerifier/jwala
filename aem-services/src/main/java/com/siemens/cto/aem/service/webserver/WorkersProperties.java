package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representation of workers.properties settings.
 * And yes it's WorkersProperties with a 's' in reference to
 * the 'workers.properties' file.
 *
 * Created by Z003BPEJ on 6/24/14.
 */
public class WorkersProperties {
    private final List<Jvm> jvms;
    private final String loadBalancerPortType;
    private final static String LOAD_BALANCER_PREFIX = "lb";
    private final List<String> loadBalancerNames = new ArrayList<>();
    private final int stickySession;
    private final String loadBalancerType;
    private final String statusCssPath;
    private final static String WORKER_PREFIX = "worker";

    private WorkersProperties(final List<Jvm> aJvms,
                              final String aLoadBalancerPortType,
                              final List<Application> apps,
                              final String aLoadBalancerType,
                              final int aStickySession,
                              final String aStatusCssPath) {
        jvms = aJvms;
        loadBalancerPortType = aLoadBalancerPortType;
        loadBalancerType = aLoadBalancerType;
        stickySession = aStickySession;
        statusCssPath = aStatusCssPath;

        for (Application app : apps) {
            loadBalancerNames.add(LOAD_BALANCER_PREFIX + "-" + app.getName());
        }
    }

    private final StringBuilder createWorkerList() {
        final StringBuilder sb = new StringBuilder("worker.list=status");
        for (String lb : loadBalancerNames) {
            sb.append(",").append(lb);
        }
        return sb.append("\n\n");
    }

    private final StringBuilder createJvmSettings() {
        final StringBuilder sb = new StringBuilder();
        for (Jvm jvm : jvms) {
            sb.append(WORKER_PREFIX).append('.').append(jvm.getJvmName()).append(".type=").append(loadBalancerPortType).append("\n");
            sb.append(WORKER_PREFIX).append('.').append(jvm.getJvmName()).append(".host=")
              .append(jvm.getHostName()).append("\n");
            sb.append(WORKER_PREFIX).append('.').append(jvm.getJvmName()).append(".port=")
              .append(jvm.getAjpPort()).append("\n\n");
        }

        return sb;
    }

    private final List<String> getJvmNames() {
        final List<String> jvmNames = new ArrayList<>();
        for (final Jvm jvm: jvms) {
            jvmNames.add(jvm.getJvmName());
        }
        return jvmNames;
    }

    private final StringBuilder createApplicationLoadBalancerSettings() {
        StringBuilder sb = new StringBuilder();
        for (String lb : loadBalancerNames) {
            sb.append(WORKER_PREFIX).append('.').append(lb).append(".type=").append(loadBalancerType).append("\n");
            sb.append(WORKER_PREFIX).append('.').append(lb).append(".balance_workers=")
              .append(StringUtils.join(getJvmNames(), ',')).append("\n");
            sb.append(WORKER_PREFIX).append('.').append(lb).append(".sticky_session=")
              .append(stickySession).append("\n\n");
        }

        return sb;
    }

    private final String createWorkerStatusSettings() {
        return "worker.status.type=status\n" +
               "worker.status.css=" + statusCssPath;
    }

    public final String toString() {
        return createWorkerList()
                .append(createJvmSettings()
                        .append(createApplicationLoadBalancerSettings()
                                .append(createWorkerStatusSettings()))).toString();
    }

    /**
     * Builder for {@link WorkersProperties}
     */
    public static class Builder {
        private List<Jvm> jvms;
        private String loadBalancerPortType;
        private List<Application> applications;
        private int stickySession;
        private String loadBalancerType;
        private String statusCssPath;

        public final Builder setJvms(final List<Jvm> aJvms) {
            jvms = aJvms;
            return this;
        }

        public Builder setLoadBalancerPortType(String loadBalancerPortType) {
            this.loadBalancerPortType = loadBalancerPortType;
            return this;
        }

        public final Builder setApps(final List<Application> apps) {
            applications = apps;
            return this;
        }

        public final Builder setLoadBalancerType(final String aLoadBalancerType) {
            loadBalancerType = aLoadBalancerType;
            return this;
        }

        public final Builder setStickySession(final int aStickySession) {
            stickySession = aStickySession;
            return this;
        }

        public final Builder setStatusCssPath(final String aStatusCssPath) {
            statusCssPath = aStatusCssPath;
            return this;
        }

        public final WorkersProperties build() {
            WorkersProperties workersProperties = new WorkersProperties(jvms,
                                                                     loadBalancerPortType,
                                                                     applications,
                                                                     loadBalancerType,
                                                                     stickySession,
                                                                     statusCssPath);

            return workersProperties;
        }

    }

}