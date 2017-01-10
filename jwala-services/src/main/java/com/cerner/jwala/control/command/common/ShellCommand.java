package com.cerner.jwala.control.command.common;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.service.RemoteCommandReturnInfo;

/**
 * The ApplicationCommand functional interface.<br/>
 */
@FunctionalInterface
public interface ShellCommand<T> {
    public RemoteCommandReturnInfo apply(String host, String... params);
}
