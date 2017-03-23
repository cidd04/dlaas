package com.equinix.dlaas.api.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ransay on 2/20/2017.
 */
public class Errors {

    private int status;

    private String message;

    private List<Error> errors = new ArrayList<>();

    public Errors(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void addError(String path, String message) {
        Error error = new Error(path, message);
        errors.add(error);
    }

    public List<Error> getErrors() {
        return errors;
    }
}