package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.jsch.JschChannelPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link JschChannelPool} implementation.
 *
 * Created by JC043760 on 2/11/2016.
 */
public class JschChannelPoolImpl implements JschChannelPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschChannelPoolImpl.class);
    private Map<Integer, Boolean> channelAvailabilityMap = new HashMap<>();
    private Map<Integer, Channel> channelMap = new HashMap<>();

    public JschChannelPoolImpl(final int poolSize, final Session session, final String channelType) {
        try {
            for (int i = 0; i < poolSize; i++) {
                final Channel newChannel = session.openChannel(channelType);
                channelMap.put(newChannel.getId(), newChannel);
                channelAvailabilityMap.put(newChannel.getId(), true);
            }
        } catch (final JSchException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    @Override
    // TODO: Optimize, find a way to eliminate the loop, or do something clever.
    public synchronized Channel borrowChannel() {
        for (final Integer id : channelAvailabilityMap.keySet()) {
            if (channelAvailabilityMap.get(id)) {
                LOGGER.debug("Channel {} was borrowed", id);
                channelAvailabilityMap.put(id, false);
                return channelMap.get(id);
            }
        }
        LOGGER.warn("There are no channels available as of the moment!");
        return null; // Sorry! Try again later!
    }

    @Override
    // TODO: Optimize, find a way to eliminate the loop, or do something clever.
    public synchronized void returnChannel(final Channel channel) {
        LOGGER.debug("Returning channel {}", channel.getId());
        if (channelAvailabilityMap.containsKey(channel.getId())) {
            channelAvailabilityMap.put(channel.getId(), true);
            LOGGER.debug("Channel {} returned.", channel.getId());
        } else {
            LOGGER.warn("Channel {} is not in the pool!", channel.getId());
        }
    }

}
