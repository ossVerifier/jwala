package com.siemens.cto.aem.commandprocessor.impl.jsch.impl.spring.component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.commandprocessor.jsch.JschChannelPool;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschChannelPoolImpl;
import com.siemens.cto.aem.commandprocessor.jsch.JschChannelService;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * JSCH Channel Service.
 *
 * Created by JC043760 on 2/9/2016.
 */
@Service("jschChannelService")
public class JschChannelServiceImpl implements JschChannelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschChannelServiceImpl.class);
    public static final int CHANNEL_POOL_SIZE = 10;
    private static final Map<String, Session> SESSION_MAP = new HashMap<>();
    private static final Map<String, JschChannelPool> CHANNEL_POOL_MAP = new HashMap<>();

    public JschChannelServiceImpl() {
        LOGGER.info("JschChannelServiceImpl instantiated!");
    }

    /**
     * Get a channel from a pool of sessions and channels. If a session is not available it is created along with its
     * channels.
     *
     * @param jsch {@link JSch}
     * @param remoteSystemConnection contains connection details
     * @param channelType the channel type e.g. shell, exec
     * @return {@link Channel}
     * @throws JSchException
     */
    @Override
    public Channel getChannel(final JSch jsch, final RemoteSystemConnection remoteSystemConnection, final String channelType) throws JSchException {
        synchronized (remoteSystemConnection.getHost()) {
            LOGGER.debug("Entering getChannel host = {}", remoteSystemConnection.getHost());
            final String key =  channelType + remoteSystemConnection.getHost();
            Session session = SESSION_MAP.get(key);
            if (session == null || !session.isConnected()) {
                session = createAndStoreSession(jsch, remoteSystemConnection, channelType);
                LOGGER.debug("Session with key = {} created!", key);
                CHANNEL_POOL_MAP.put(key, new JschChannelPoolImpl(CHANNEL_POOL_SIZE, session, channelType));
                LOGGER.info("Channel pool for host = {} and channel type = {} created!", remoteSystemConnection.getHost(), channelType);
            }
            LOGGER.debug("Exiting getChannel host = {}", remoteSystemConnection.getHost());
            return CHANNEL_POOL_MAP.get(key).borrowChannel();
        }
    }

    /**
     * Return a channel.
     *
     * @param host the host name
     * @param channel {@link Channel}
     * @param channelType Channel type e.g. shell, exec
     */
    @Override
    public synchronized void returnChannel(final String host, final Channel channel, final String channelType) {
        CHANNEL_POOL_MAP.get(channelType + host).returnChannel(channel);
    }

    /**
     * Crate a session.
     *
     * @param jsch {@link JSch}
     * @param remoteSystemConnection contains connection information
     * @param channelType the channel type e.g. shell, exec
     * @return {@link Session}
     * @throws JSchException
     */
    private Session createAndStoreSession(final JSch jsch, final RemoteSystemConnection remoteSystemConnection,
                                          final String channelType) throws JSchException {
        final Session newSession = prepareSession(jsch, remoteSystemConnection);
        LOGGER.info("New session created!");
        SESSION_MAP.put(channelType + remoteSystemConnection.getHost(), newSession);
        newSession.connect();
        LOGGER.info("Session connected!");
        return newSession;
    }

    /**
     * Prepare the session by setting session properties.
     * @param jsch {@link JSch}
     * @param remoteSystemConnection contains connection information
     * @return {@link Session}
     * @throws JSchException
     */
    private Session prepareSession(final JSch jsch, final RemoteSystemConnection remoteSystemConnection)  throws JSchException {
        final Session session = jsch.getSession(remoteSystemConnection.getUser(), remoteSystemConnection.getHost(),
                remoteSystemConnection.getPort());
        final String password = remoteSystemConnection.getPassword();
        if (password != null) {
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password,gssapi-with-mic,publickey,keyboard-interactive");
        }
        return session;
    }

}
