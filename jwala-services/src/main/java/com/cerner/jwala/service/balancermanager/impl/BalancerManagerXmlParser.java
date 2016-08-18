package com.cerner.jwala.service.balancermanager.impl;

import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.service.balancermanager.impl.xml.data.Manager;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalancerManagerXmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancerManagerXmlParser.class);

    public String getUrlPath(final String host, final String balancerName, final String nonce) {
        return "https://" + host + "/balancer-manager" + "?b=" + balancerName + "&xml=1&nonce=" + nonce;
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

    public Map<String, String> getWorkers(final Manager manager, final String balancerName) {
        LOGGER.info("Entering getWorkers for application: ");
        Map<String, String> workers = new HashMap<>();
        for (Manager.Balancer balancers : manager.getBalancers()) {
            if (balancers.getName().equalsIgnoreCase("balancer://" + balancerName)) {
                for (Manager.Balancer.Worker worker : balancers.getWorkers()) {
                    workers.put(worker.getName(), worker.getRoute());
                }
            }
        }
        return workers;
    }
}
