package com.cerner.jwala.service.jvm.operation.impl;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.service.jvm.operation.Operation;

/**
 * The list commands that can be executed in a server
 *
 * Created by Jedd Cuison on 12/16/2016
 */
public enum Operations {

    /**
     *
     */
    START(new Start()), STOP(new Stop());
    /**
     *
     */
    private Operation op;

    Operations(final Operation op) {
        this.op = op;
    }

    public void run(Jvm jvm) {
        op.execute(jvm);
    }

}
