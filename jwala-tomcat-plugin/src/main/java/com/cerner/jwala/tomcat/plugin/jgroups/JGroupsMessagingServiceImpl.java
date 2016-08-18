package com.cerner.jwala.tomcat.plugin.jgroups;

import com.cerner.jwala.tomcat.plugin.MessagingService;
import com.cerner.jwala.tomcat.plugin.MessagingServiceException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link MessagingService} via JGroups
 *
 * Created by JC043760 on 8/15/2016
 */
public class JGroupsMessagingServiceImpl implements MessagingService<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupsMessagingServiceImpl.class);

    private final String configXml;
    private final String clusterName;
    private final boolean discardOwnMessages;

    public JChannel channel;

    public JGroupsMessagingServiceImpl(final String configXml, final String clusterName, final boolean discardOwnMessages) {
        this.configXml = configXml;
        this.clusterName = clusterName;
        this.discardOwnMessages = discardOwnMessages;
    }

    @Override
    public void init() {
        try {
            LOGGER.info("Initializing JGroups channel...");
            channel = new JChannel(configXml);
            channel.setDiscardOwnMessages(true);
            connect(clusterName);
        } catch (final Exception e) {
            throw new MessagingServiceException("Failed to initialize the service!", e);
        }
    }

    @Override
    public synchronized void send(final Message msg) {
        try {
            if (msg != null) {
                connect(clusterName);
                LOGGER.info("Sending msg {}", msg);
                LOGGER.info("Msg content = {}", msg.getObject());
                channel.send(msg);
            } else {
                LOGGER.warn("Cannot send null msg!");
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new MessagingServiceException("Failed to deliver message!", e);
        }
    }

    @Override
    public void destroy() {
        if (channel.isConnected()) {
            LOGGER.info("Closing channel connection...");
            channel.close();
            LOGGER.info("Channel closed");
        }
    }

    /**
     * Connect channel if it's not already connected
     * @param clusterName the cluster to connect to
     * @throws Exception the exception
     */
    private void connect(final String clusterName) throws Exception {
        if (!channel.isConnected())  {
            LOGGER.info("Connecting to JGroups cluster {} using configuredProperties {}", clusterName, this.toString());
            channel.connect(clusterName);
            LOGGER.info("Channel connected");
        }
    }

    public JChannel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "JGroupsMessagingServiceImpl{" +
                "configXml='" + configXml + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", discardOwnMessages=" + discardOwnMessages +
                '}';
    }
}
