package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.Channel;

/**
 * JSCH pool contract.
 *
 * Created by JC043760 on 2/12/2016.
 */
public interface JschChannelPool {

    /**
     * Borrow a channel from the pool.
     * @return returns a {@link Channel} or null if non is available.
     */
    Channel borrowChannel();

    /**
     * Returns a channel to the pool.
     * @param channel {@link Channel}
     */
    void returnChannel(Channel channel);

}
