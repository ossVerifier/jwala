package com.siemens.cto.aem.service.initializer;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.service.exception.JGroupsClusterInitializerException;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.PhysicalAddress;
import org.jgroups.ReceiverAdapter;
import org.jgroups.stack.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * JGroups cluster initializer.
 * <p/>
 * Created by JC043760 on 3/15/2016.
 */
public class JGroupsClusterInitializer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupsClusterInitializer.class);

    private final String jgroupsJavaNetPreferIPv4Stack = ApplicationProperties.get("jgroups.java.net.preferIPv4Stack", "true");
    private final String jgroupsCoordinatorIPAddress = ApplicationProperties.get("jgroups.coordinator.ip.address");
    private final String jgroupsCoordinatorPort = ApplicationProperties.get("jgroups.coordinator.port");
    private final String jgroupsClusterConnectTimeout = ApplicationProperties.get("jgroups.cluster.connect.timeout", "10000");
    private final String jgroupsClusterName = ApplicationProperties.get("jgroups.cluster.name", "DefaultTOCCluster");
    private final String jgroupsConfXml = ApplicationProperties.get("jgroups.conf.xml", "tcp.xml");
    private final ReceiverAdapter receiverAdapter;

    public JGroupsClusterInitializer(final ReceiverAdapter receiverAdapter) {
        this.receiverAdapter = receiverAdapter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", jgroupsJavaNetPreferIPv4Stack);

        try {
            LOGGER.debug("Starting JGroups cluster {}", jgroupsClusterName);
            final JChannel channel = new JChannel(jgroupsConfXml);
            channel.setReceiver(receiverAdapter);

            IpAddress coordinatorIP = new IpAddress(this.jgroupsCoordinatorIPAddress + ":" + jgroupsCoordinatorPort);
            channel.connect(jgroupsClusterName, coordinatorIP, Long.parseLong(jgroupsClusterConnectTimeout));
            LOGGER.debug("JGroups connection to cluster {} SUCCESSFUL", jgroupsClusterName);

            PhysicalAddress physicalAddr = (PhysicalAddress) channel.down(new Event(Event.GET_PHYSICAL_ADDRESS, channel.getAddress()));
            LOGGER.info("JGroups cluster physical address {} {} {}", jgroupsClusterName, channel.getName(), physicalAddr);
        } catch (Exception e) {
            LOGGER.error("FAILURE using JGroups: could not connect to cluster {}", jgroupsClusterName, e);
            throw new JGroupsClusterInitializerException("JGroups cluster initialization failed!", e);
        }
    }
}
