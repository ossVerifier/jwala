package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import com.cerner.jwala.tomcat.listener.messaging.MessagingService;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.stack.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A life cycle listener that sends state via JGroups
 *
 * Created by JC043760 on 8/15/2016
 */
public class JGroupsReportingLifeCycleListener implements LifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupsReportingLifeCycleListener.class);
    private static final long SCHEDULER_DELAY_INITIAL_DEFAULT = 60;
    private static final long SCHEDULER_DELAY_SUBSEQUENT_DEFAULT = 60;
    private static final int SCHEDULER_THREAD_COUNT_DEFAULT = 1;
    private MessagingService<Message> messagingService;
    private JGroupsStateReporter jgroupsStateReporter;

    private String serverId;
    private String serverName;
    private String jgroupsPreferIpv4Stack;
    private String jgroupsConfigXml;
    private String jgroupsCoordinatorIp;
    private String jgroupsCoordinatorPort;
    private String jgroupsClusterName;
    private long schedulerDelayInitial = SCHEDULER_DELAY_INITIAL_DEFAULT;
    private long schedulerDelaySubsequent = SCHEDULER_DELAY_SUBSEQUENT_DEFAULT;
    private TimeUnit schedulerDelayUnit = TimeUnit.SECONDS;
    private int schedulerThreadCount = SCHEDULER_THREAD_COUNT_DEFAULT;

    private JChannel channel;

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        LOGGER.info("LifeCycleEvent received: {} on {}", event.getType(), event.getLifecycle().getStateName());
        if (messagingService == null) {
            // init messaging service...
            LOGGER.info("Set systems property java.net.preferIPv4Stack to '{}'", jgroupsPreferIpv4Stack);
            System.setProperty("java.net.preferIPv4Stack", jgroupsPreferIpv4Stack);

            try {
                channel = new JChannel(jgroupsConfigXml);
            } catch (final Exception e) {
                LOGGER.error("Failed to create JGroups channel!", e);
                return;
            }

            channel.setDiscardOwnMessages(true);
            messagingService = new JGroupsMessagingServiceImpl(channel, jgroupsClusterName);
            jgroupsStateReporter = new JGroupsStateReporter(messagingService);
        }

        final LifecycleState state = event.getLifecycle().getState();
        try {
            jgroupsStateReporter.init(state)
                                .sendMsg(serverId, serverName, new IpAddress(jgroupsCoordinatorIp + ":" + jgroupsCoordinatorPort))
                                .schedulePeriodicMsgDelivery(schedulerThreadCount, schedulerDelayInitial,
                                        schedulerDelaySubsequent, schedulerDelayUnit);
        } catch (final Exception e) {
            LOGGER.error("Failed to report state!", e);
        }
    }

    public void setServerId(final String serverId) {
        this.serverId = serverId;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public void setJgroupsPreferIpv4Stack(String jgroupsPreferIpv4Stack) {
        this.jgroupsPreferIpv4Stack = jgroupsPreferIpv4Stack;
    }

    public void setJgroupsConfigXml(final String jgroupsConfigXml) {
        this.jgroupsConfigXml = jgroupsConfigXml;
    }

    public void setJgroupsCoordinatorIp(final String jgroupsCoordinatorIp) {
        this.jgroupsCoordinatorIp = jgroupsCoordinatorIp;
    }

    public void setJgroupsCoordinatorPort(final String jgroupsCoordinatorPort) {
        this.jgroupsCoordinatorPort = jgroupsCoordinatorPort;
    }

    public void setJgroupsClusterName(final String jgroupsClusterName) {
        this.jgroupsClusterName = jgroupsClusterName;
    }

    public void setSchedulerDelayInitial(final String schedulerDelayInitial) {
        try {
            this.schedulerDelayInitial = Long.parseLong(schedulerDelayInitial);
        } catch (final NumberFormatException e) {
            LOGGER.error("Failed to convert schedulerDelayInitial value of \"{}\" to long! " +
                    "The default value {} will be used instead.", schedulerDelayInitial, SCHEDULER_DELAY_INITIAL_DEFAULT);
        }
    }

    public void setSchedulerDelaySubsequent(final String schedulerDelaySubsequent) {
        try {
            this.schedulerDelaySubsequent = Long.parseLong(schedulerDelaySubsequent);
        } catch (final NumberFormatException e) {
            LOGGER.error("Failed to convert schedulerDelaySubsequent value of \"{}\" to long! " +
                    "The default value {} will be used instead.", schedulerDelaySubsequent, SCHEDULER_DELAY_SUBSEQUENT_DEFAULT);
        }
    }

    public void setSchedulerDelayUnit(final String schedulerDelayUnit) {
        try {
            this.schedulerDelayUnit = TimeUnit.valueOf(schedulerDelayUnit);
        } catch (final IllegalArgumentException e) {
            LOGGER.error("Invalid schedulerDelayUnit value \"{}\"! The default value of {} will be used instead.", TimeUnit.SECONDS);
        }
    }

    public void setSchedulerThreadCount(final String schedulerThreadCount) {
        try {
            this.schedulerThreadCount = Integer.parseInt(schedulerThreadCount);
        } catch (final NumberFormatException e) {
            LOGGER.error("Failed to convert schedulerThreadCount value of \"{}\" to integer! " +
                    "The default value {} will be used instead.", schedulerDelaySubsequent, SCHEDULER_THREAD_COUNT_DEFAULT);
        }
    }
}
