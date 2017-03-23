package com.equinix.dlaas.api.controller;

/**
 * Created by ransay on 2/5/2017.
 */

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.equinix.dlaas.api.domain.Errors;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class EventErrorHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public Errors methodArgumentNotValidException(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            BindingResult result = ((MethodArgumentNotValidException) ex).getBindingResult();
            List<org.springframework.validation.FieldError> fieldErrors = result.getFieldErrors();
            return processFieldErrors(fieldErrors);
        } else if (ex instanceof BindException) {
            BindingResult result = ((BindException) ex).getBindingResult();
            List<org.springframework.validation.FieldError> fieldErrors = result.getFieldErrors();
            return processFieldErrors(fieldErrors);
        } else {
            return new Errors(0, ex.getMessage());
        }
    }

    private Errors processFieldErrors(List<org.springframework.validation.FieldError> fieldErrors) {
        Errors errors = new Errors(BAD_REQUEST.value(), "validation error");
        for (org.springframework.validation.FieldError fieldError: fieldErrors) {
            errors.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }

}
