package com.cerner.jwala.common.jsch;

import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;

/**
 * Defines a rudimentary JSCH service
 *
 * Created by Jedd Cuison on 12/23/2016
 */
public interface JschService {

    Session prepareSession(final RemoteSystemConnection remoteSystemConnection) throws JSchException;

    RemoteCommandReturnInfo runCommand(String command, Channel channel, long timeout) throws IOException, JSchException;

}
