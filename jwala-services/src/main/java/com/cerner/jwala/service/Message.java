package com.cerner.jwala.service;

/**
 * Message template that is sent via {@link MessagingService}
 *
 * Created by JC043760 on 11/2/2016
 */
public class Message<R, B> {

    private R recipient;
    private String subject;
    private B body;

    public Message(final R recipient, final String subject, final B body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    public R getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public B getBody() {
        return body;
    }
}
