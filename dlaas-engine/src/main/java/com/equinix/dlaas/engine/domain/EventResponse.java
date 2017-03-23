package com.equinix.dlaas.engine.domain;

/**
 * Created by ransay on 2/7/2017.
 */
public class EventResponse {

    private String message;
    //If any
    private Errors errors;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }
}
