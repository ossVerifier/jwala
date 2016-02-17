package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;

/**
 * JSCH Channel Service contract.
 *
 * Created by JC043760 on 2/17/2016.
 */
public interface JschChannelService {

    Channel getChannel(final JSch jsch, final RemoteSystemConnection remoteSystemConnection, final String channelType) throws JSchException;

    void returnChannel(final String host, final Channel channel, final String channelType);

}
