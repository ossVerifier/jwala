package com.cerner.jwala.tomcat.listener.messaging;

/**
 * JVM states as defined by Jwala
 *
 * Note: IMHO Tomcat states should be delivered to the receiver (Jwala) "as is".
 *       Interpretation/conversion should be the responsibility of the receiver.
 *       This enum was implemented in the TIM version of JGroups life cycle listener
 *       albeit with a different name. It was used here to minimized the impact of
 *       changing the receiver's code base.
 *
 * Created by JC043760 on 8/16/2016.
 */
public enum JwalaJvmState {
    JVM_INITIALIZING, JVM_INITIALIZED, JVM_FAILED, JVM_STARTING, JVM_STARTED, JVM_STOPPING, JVM_STOPPED, JVM_DESTROYING,
    JVM_DESTROYED
}
