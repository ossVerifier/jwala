package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import com.cerner.jwala.tomcat.listener.messaging.MessagingService;
import org.apache.catalina.LifecycleState;
import org.jgroups.Address;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The reporting mechanism
 *
 * Usage: jgroupsStateReporter.init().sendMsg(...).schedulePeriodicMsgDelivery(...)
 *
 * Created by Jedd Cuison on 8/18/2016.
 */
public class JGroupsStateReporter {

    public static final Logger LOGGER = LoggerFactory.getLogger(JGroupsStateReporter.class);

    private ScheduledExecutorService scheduler;
    private final MessagingService<Message> messagingService;
    private LifecycleState state;
    private JGroupsServerInfoMessageBuilder msgBuilder;

    public JGroupsStateReporter(final MessagingService<Message> messagingService) {
        this.messagingService = messagingService;
    }

    /**
     * Destroy the scheduler on new state
     * @param newState the latest state
     * @return {@link JGroupsStateReporter} for chaining purposes
     */
    public JGroupsStateReporter init(final LifecycleState newState) {
        if (scheduler != null && !newState.equals(state)) {
            LOGGER.info("Shutting down the scheduler NOW...");
            scheduler.shutdownNow();
            scheduler = null;
        }
        state = newState;
        return this;
    }

    /**
     * Create a message and send it
     * @param serverId the server instance id
     * @param serverName the server name
     *@param destAddr the JGroups destination address  @return {@link JGroupsStateReporter} for chaining purposes
     */
    public JGroupsStateReporter sendMsg(final String serverId, final String serverName, final Address destAddr) {
        synchronized (messagingService) {
            messagingService.init();
            try {
                final Address channelAddress = ((JGroupsMessagingServiceImpl) messagingService).getChannel().getAddress();
                msgBuilder = new JGroupsServerInfoMessageBuilder().setServerId(serverId)
                                                        .setServerName(serverName)
                                                        .setState(state)
                                                        .setSrcAddress(channelAddress)
                                                        .setDestAddress(destAddr);
            } catch (final Exception e) {
                throw new JGroupsStateReporterException("Failed to create message!", e);
            }
            messagingService.send(msgBuilder.build()); // send the state details immediately
            if (LifecycleState.STOPPED.equals(state) || LifecycleState.DESTROYED.equals(state)) {
                LOGGER.info("State {} received, destroying messaging service...", state);
                messagingService.destroy();
                return this;
            }
        }
        return this;
    }

    /**
     * Schedule periodic message delivery
     * @param schedulerThreadCount the thread count
     * @param schedulerDelayInitial the scheduler's initial delay
     * @param schedulerDelaySubsequent the scheduler's subsequent delay
     * @param schedulerDelayUnit {@link TimeUnit} the delay unit
     * @return {@link JGroupsStateReporter} for chaining purposes
     */
    public JGroupsStateReporter schedulePeriodicMsgDelivery(final int schedulerThreadCount, final long schedulerDelayInitial,
                                                            final long schedulerDelaySubsequent, final TimeUnit schedulerDelayUnit) {
        if (((JGroupsMessagingServiceImpl) messagingService).getChannel().isConnected() && scheduler == null) {
            LOGGER.info("Creating scheduler with treadCount: {}, initialDelay: {}, subsequentDelay: {} and timeUnit: {}",
                        schedulerThreadCount, schedulerDelayInitial, schedulerDelaySubsequent, schedulerDelayUnit);
            scheduler = Executors.newScheduledThreadPool(schedulerThreadCount);
            scheduler.scheduleAtFixedRate(new JGroupsLifeCycleReporterRunnable(messagingService, msgBuilder), schedulerDelayInitial,
                    schedulerDelaySubsequent, schedulerDelayUnit);
        }
        return this;
    }
}
