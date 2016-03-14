package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.common.domain.model.id.Identifier;

/**
 * Retrieve group state details (e.g. running JVM count and Web Sever count) and send the said data to a destination via
 * a messaging system like JMS or Spring STOMP.
 *
 * Created by JC043760 on 3/14/2016.
 */
public interface GroupStateNotificationService {

    /**
     * Retrieve the group state and send it to a topic.
     * @param id
     * @param aClass
     * @param topic
     */
    void retrieveStateAndSendToATopic(Identifier id, Class aClass, String topic);

}
