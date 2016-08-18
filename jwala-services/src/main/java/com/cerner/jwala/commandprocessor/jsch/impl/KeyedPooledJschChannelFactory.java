package com.cerner.jwala.commandprocessor.jsch.impl;

import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A keyed JSCH channel factory.
 *
 * Created by JC043760 on 2/26/2016.
 */
public class KeyedPooledJschChannelFactory extends BaseKeyedPooledObjectFactory<ChannelSessionKey, Channel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyedPooledJschChannelFactory.class);

    private final JSch jsch;
    private static final Map<ChannelSessionKey, Session> SESSION_MAP = new HashMap<>();

    public KeyedPooledJschChannelFactory(final JSch jsch) {
        this.jsch = jsch;
    }

    @Override
    public Channel create(final ChannelSessionKey key) throws Exception {
        Session session = SESSION_MAP.get(key);
        if (session == null || !session.isConnected()) {
            session = prepareSession(key.remoteSystemConnection);
            session.connect();
            SESSION_MAP.put(key, session);
            LOGGER.debug("session {} created and connected!", key);
        }
        return session.openChannel(key.channelType.getChannelType());
    }

    @Override
    public PooledObject<Channel> wrap(final Channel channel) {
        return new DefaultPooledObject<>(channel);
    }

    @Override
    public void destroyObject(final ChannelSessionKey key, final PooledObject<Channel> pool) throws Exception {
        pool.getObject().disconnect();
        LOGGER.debug("channel {} of session {} disconnected!", pool.getObject().getId(), key);
        super.destroyObject(key, pool);
    }

    /**
     * Prepare the session by setting session properties.
     * @param remoteSystemConnection {@link RemoteSystemConnection}
     * @return {@link Session}
     * @throws JSchException
     */
    private Session prepareSession(final RemoteSystemConnection remoteSystemConnection)  throws JSchException {
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
