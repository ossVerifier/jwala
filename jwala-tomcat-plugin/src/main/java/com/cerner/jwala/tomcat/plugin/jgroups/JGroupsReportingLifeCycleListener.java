package com.cerner.jwala.tomcat.plugin.jgroups;

import com.cerner.jwala.tomcat.plugin.JwalaJvmState;
import com.cerner.jwala.tomcat.plugin.MessagingService;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.stack.IpAddress;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    public static final String JVM_MSG_TYPE = "JVM";
    private MessagingService<Message> messagingService;

    private String instanceId;
    private String jgroupsPreferIpv4Stack;
    private String jgroupsConfigXml;
    private String jgroupsCoordinatorIp;
    private String jgroupsCoordinatorPort;
    private String jgroupsClusterName;
    private long schedulerDelayInitial = SCHEDULER_DELAY_INITIAL_DEFAULT;
    private long schedulerDelaySubsequent = SCHEDULER_DELAY_SUBSEQUENT_DEFAULT;
    private TimeUnit schedulerDelayUnit = TimeUnit.SECONDS;
    private int schedulerThreadCount = SCHEDULER_THREAD_COUNT_DEFAULT;

    private ScheduledExecutorService scheduler;

    private String lastState;

    private final static Map<LifecycleState, JwalaJvmState> LIFECYCLE_JWALA_JVM_STATE_REF_MAP = new HashMap<>();

    static {
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.DESTROYED, JwalaJvmState.JVM_STOPPED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.DESTROYING, JwalaJvmState.JVM_STOPPED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.FAILED, JwalaJvmState.JVM_FAILED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.INITIALIZED, JwalaJvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.INITIALIZING, JwalaJvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.MUST_DESTROY, JwalaJvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.MUST_STOP, JwalaJvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.NEW, JwalaJvmState.JVM_INITIALIZED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTED, JwalaJvmState.JVM_STARTED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTING, JwalaJvmState.JVM_STARTING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STARTING_PREP, JwalaJvmState.JVM_STARTING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPED, JwalaJvmState.JVM_STOPPED);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPING, JwalaJvmState.JVM_STOPPING);
        LIFECYCLE_JWALA_JVM_STATE_REF_MAP.put(LifecycleState.STOPPING_PREP, JwalaJvmState.JVM_STOPPING);
    }

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        LOGGER.info("LifeCycleEvent received: {} on {}", event.getType(), event.getLifecycle().getStateName());

        if (messagingService == null) {
            // init messaging service...
            LOGGER.info("Set systems property java.net.preferIPv4Stack to '{}'", jgroupsPreferIpv4Stack);
            System.setProperty("java.net.preferIPv4Stack", jgroupsPreferIpv4Stack);

            try {
                messagingService = new JGroupsMessagingServiceImpl(jgroupsConfigXml, jgroupsClusterName, true);
            } catch (final Exception e) {
                LOGGER.error("Failed to initialize messaging service!", e);
                return;
            }
        }

        reportCurrentState(LIFECYCLE_JWALA_JVM_STATE_REF_MAP.get(LifecycleState.valueOf(event.getLifecycle().getStateName())).toString());
    }

    /**
     * Report the state via JGroup channel
     * @param state the state
     */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    private void reportCurrentState(final String state) {
        if (scheduler != null && !state.equalsIgnoreCase(lastState)) {
            shutdownScheduler();
            scheduler = null;
        }

        final JGroupsMessageBuilder msgBuilder;
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
                                                        .setDestAddress(new IpAddress(jgroupsCoordinatorIp + ":" + jgroupsCoordinatorPort));
            } catch (final Exception e) {
                LOGGER.error("Failed to create message!", e);
                return;
            }
            messagingService.send(msgBuilder.build()); // send the state details immediately
            if (JwalaJvmState.JVM_STOPPED.name().equalsIgnoreCase(state)) {
                LOGGER.info("Stop JVM state was received, destroying messaging service...");
                messagingService.destroy();
                return;
            }
        }

        lastState = state;

        // periodic state details sending while there are no new state received
        if (scheduler == null) {
            LOGGER.info("Creating scheduler...");
            scheduler = Executors.newScheduledThreadPool(schedulerThreadCount);
            scheduler.scheduleAtFixedRate(new JGroupsLifeCycleReporterRunnable(messagingService, msgBuilder), schedulerDelayInitial,
                    schedulerDelaySubsequent, schedulerDelayUnit);
        }
    }

    /**
     * Shutdown the scheduler and wait until the running thread is done or has been cancelled
     */
    private void shutdownScheduler() {
        LOGGER.info("Shutting down the scheduler...");
        scheduler.shutdownNow();
    }

    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
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
