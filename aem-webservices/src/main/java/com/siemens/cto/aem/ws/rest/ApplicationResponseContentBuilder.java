package com.siemens.cto.aem.ws.rest;

/**
 * Created by z003bpej on 2/21/14.
 */
public class ApplicationResponseContentBuilder<T> {

    private final T content;

    public ApplicationResponseContentBuilder(T content) {
        this.content = content;
    }

    public ApplicationResponseContent build() {
        return new ApplicationResponseContent() {
            public T getContent() {
                return content;
            }
        };
    }

}
