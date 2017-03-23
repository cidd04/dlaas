package com.equinix.dlaas.engine.domain;

/**
 * Created by ransay on 2/20/2017.
 */
public class Error {

    private String field;

    private String defaultMessage;

    public Error(String field, String defaultMessage) {
        this.field = field;
        this.defaultMessage = defaultMessage;
    }

    public String getField() {
        return this.field;
    }

    public Object getDefaultMessage() {
        return this.defaultMessage;
    }
}