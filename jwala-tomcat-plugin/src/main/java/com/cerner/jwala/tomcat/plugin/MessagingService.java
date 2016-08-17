package com.cerner.jwala.tomcat.plugin;

/**
 * A contract that outlines what a messaging service can and should do
 *
 * Created by JC043760 on 8/15/2016
 */
public interface MessagingService<T> {

    void init();

    void send(T msg);

    void destroy();
}
