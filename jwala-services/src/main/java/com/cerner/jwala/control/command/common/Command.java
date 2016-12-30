package com.cerner.jwala.control.command.common;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.service.RemoteCommandReturnInfo;

/**
 * The Command functional interface.<br/>
 */
@FunctionalInterface
public interface Command<T> {
    public RemoteCommandReturnInfo apply(Jvm jvm);
}
