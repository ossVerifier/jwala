package com.cerner.jwala.service.jvm.operation;

import com.cerner.jwala.common.domain.model.jvm.Jvm;

/**
 * The primary interface to implement the command design pattern for sending remote commands to a server
 *
 * Created by Jedd Cuison on 12/16/2016
 */
public interface Operation {

    void execute(Jvm jvm);

}
