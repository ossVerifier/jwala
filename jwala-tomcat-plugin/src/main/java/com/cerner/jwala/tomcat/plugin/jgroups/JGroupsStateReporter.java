package com.cerner.jwala.tomcat.plugin.jgroups;

import com.cerner.jwala.tomcat.plugin.JwalaJvmState;
import com.cerner.jwala.tomcat.plugin.MessagingService;
import org.jgroups.Address;
import org.jgroups.Message;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
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
 * Created by JC043760 on 8/18/2016.
 */
public class JGroupsStateReporter {

    public static final Logger LOGGER = LoggerFactory.getLogger(JGroupsStateReporter.class);

    private static final String JVM_MSG_TYPE = "JVM";

    private ScheduledExecutorService scheduler;
    private final MessagingService<Message> messagingService;
    private String state;
    private JGroupsMessageBuilder msgBuilder;

    public JGroupsStateReporter(final MessagingService<Message> messagingService) {
        this.messagingService = messagingService;
    }

    /**
     * Destroy the scheduler on new state
     * @param newState the latest state
     * @return {@link JGroupsStateReporter} for chaining purposes
     */
    public JGroupsStateReporter init(final String newState) {
        if (scheduler != null && !newState.equalsIgnoreCase(state)) {
            LOGGER.info("Shutting down the scheduler NOW...");
            scheduler.shutdownNow();
            scheduler = null;
        }
        state = newState;
        return this;
    }

    /**
     * Create a message and send it
     * @param instanceId the JVM instance id
     * @param destAddr the JGroups destination address
     * @return {@link JGroupsStateReporter} for chaining purposes
     */
    public JGroupsStateReporter sendMsg(final String instanceId, final Address destAddr) {
        synchronized (messagingService) {
            messagingService.init();
            try {
                final Address channelAddress = ((JGroupsMessagingServiceImpl) messagingService).getChannel().getAddress();
                msgBuilder = new JGroupsMessageBuilder().setId(instanceId)
                        .setInstanceId(instanceId)
                        .setAsOf(ISODateTimeFormat.dateTime().print(DateTime.now()))
                        .setType(JVM_MSG_TYPE)
                        .setState(state)
                        .setSrcAddress(channelAddress)
                        .setDestAddress(destAddr);
            } catch (final Exception e) {
                throw new JGroupsStateReporterException("Failed to create message!", e);
            }
            messagingService.send(msgBuilder.build()); // send the state details immediately
            if (JwalaJvmState.JVM_STOPPED.name().equalsIgnoreCase(state)) {
                LOGGER.info("Stop JVM state was received, destroying messaging service...");
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
