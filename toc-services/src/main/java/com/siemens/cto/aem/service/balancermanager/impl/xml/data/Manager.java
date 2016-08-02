package com.siemens.cto.aem.service.balancermanager.impl.xml.data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "manager", namespace = "http://httpd.apache.org")
public class Manager {

    private List<Balancer> balancers = new ArrayList<Balancer>();


    public List<Manager.Balancer> getBalancers() {
        return this.balancers;
    }

    @XmlElementWrapper(name = "balancers", namespace = "http://httpd.apache.org")
    @XmlElement(name = "balancer", namespace = "http://httpd.apache.org")
    public void setBalancers(final List<Manager.Balancer> balancers) {
        this.balancers = balancers;
    }

    @XmlRootElement
    public static class Balancer {
        private String name;
        private String stickysession;
        private String nofailover;
        private String timeout;
        private String lbmethod;
        private String scolonpathdelim;

        private List<Worker> workers = new ArrayList<Worker>();


        public List<Balancer.Worker> getWorkers() {
            return this.workers;
        }

        @XmlElementWrapper(name = "workers", namespace = "http://httpd.apache.org")
        @XmlElement(name = "worker", namespace = "http://httpd.apache.org")
        public void setWorkers(final List<Balancer.Worker> workers) {
            this.workers = workers;
        }

        public String getName() {
            return name;
        }

        @XmlElement(name = "name", namespace = "http://httpd.apache.org")
        public void setName(String name) {
            this.name = name;
        }

        public String getStickysession() {
            return stickysession;
        }

        @XmlElement
        public void setStickysession(String stickysession) {
            this.stickysession = stickysession;
        }

        public String getNofailover() {
            return nofailover;
        }

        @XmlElement
        public void setNofailover(String nofailover) {
            this.nofailover = nofailover;
        }

        public String getTimeout() {
            return timeout;
        }

        @XmlElement
        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

        public String getLbmethod() {
            return lbmethod;
        }

        @XmlElement
        public void setLbmethod(String lbmethod) {
            this.lbmethod = lbmethod;
        }

        public String getScolonpathdelim() {
            return scolonpathdelim;
        }

        @XmlElement
        public void setScolonpathdelim(String scolonpathdelim) {
            this.scolonpathdelim = scolonpathdelim;
        }

        @XmlRootElement
        public static class Worker {
            private String name;
            private String scheme;
            private String hostname;
            private int loadfactor;
            private int port;
            private int min;
            private int smax;
            private int max;
            private int ttl;
            private String keepalive;
            private String status;
            private int retries;
            private int lbstatus;
            private int transferred;
            private int read;
            private int elected;
            private String route;
            private String redirect;
            private int busy;
            private int lbset;
            private int retry;


            public String getName() {
                return name;
            }

            @XmlElement(name = "name", namespace = "http://httpd.apache.org")
            public void setName(String name) {
                this.name = name;
            }

            public String getScheme() {
                return scheme;
            }

            @XmlElement
            public void setScheme(String scheme) {
                this.scheme = scheme;
            }

            public String getHostname() {
                return hostname;
            }

            @XmlElement
            public void setHostname(String hostname) {
                this.hostname = hostname;
            }

            public int getLoadfactor() {
                return loadfactor;
            }

            @XmlElement
            public void setLoadfactor(int loadfactor) {
                this.loadfactor = loadfactor;
            }

            public int getPort() {
                return port;
            }

            @XmlElement
            public void setPort(int port) {
                this.port = port;
            }

            public int getMin() {
                return min;
            }

            @XmlElement
            public void setMin(int min) {
                this.min = min;
            }

            public int getSmax() {
                return smax;
            }

            @XmlElement
            public void setSmax(int smax) {
                this.smax = smax;
            }

            public int getMax() {
                return max;
            }

            @XmlElement
            public void setMax(int max) {
                this.max = max;
            }

            public int getTtl() {
                return ttl;
            }

            @XmlElement
            public void setTtl(int ttl) {
                this.ttl = ttl;
            }

            public String getKeepalive() {
                return keepalive;
            }

            @XmlElement
            public void setKeepalive(String keepalive) {
                this.keepalive = keepalive;
            }

            public String getStatus() {
                return status;
            }

            @XmlElement
            public void setStatus(String status) {
                this.status = status;
            }

            public int getRetries() {
                return retries;
            }

            @XmlElement
            public void setRetries(int retries) {
                this.retries = retries;
            }

            public int getLbstatus() {
                return lbstatus;
            }

            @XmlElement
            public void setLbstatus(int lbstatus) {
                this.lbstatus = lbstatus;
            }

            public int getTransferred() {
                return transferred;
            }

            @XmlElement
            public void setTransferred(int transferred) {
                this.transferred = transferred;
            }

            public int getRead() {
                return read;
            }

            @XmlElement
            public void setRead(int read) {
                this.read = read;
            }

            public int getElected() {
                return elected;
            }

            @XmlElement
            public void setElected(int elected) {
                this.elected = elected;
            }

            public String getRoute() {
                return route;
            }

            @XmlElement(name = "route", namespace = "http://httpd.apache.org")
            public void setRoute(String route) {
                this.route = route;
            }

            public String getRedirect() {
                return redirect;
            }

            @XmlElement
            public void setRedirect(String redirect) {
                this.redirect = redirect;
            }

            public int getBusy() {
                return busy;
            }

            @XmlElement
            public void setBusy(int busy) {
                this.busy = busy;
            }

            public int getLbset() {
                return lbset;
            }

            @XmlElement
            public void setLbset(int lbset) {
                this.lbset = lbset;
            }

            public int getRetry() {
                return retry;
            }

            @XmlElement
            public void setRetry(int retry) {
                this.retry = retry;
            }
        }// End Worker
    }// End Balancer
}// End Manager
