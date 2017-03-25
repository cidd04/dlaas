package com.equinix.dlaas.api.domain;

/**
 * Created by ransay on 2/7/2017.
 */
public class EventResponse {

    //If successful, return order number
    private String orderNumber;

    private String message;
    //If any
    private Errors errors;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

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
