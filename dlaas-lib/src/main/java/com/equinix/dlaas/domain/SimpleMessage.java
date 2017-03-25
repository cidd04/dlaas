package com.equinix.dlaas.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ransay on 2/10/2017.
 */

public class SimpleMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public SimpleMessage() {}

    private SimpleMessage(SimpleMessageBuilder builder) {
        this.id = builder.id;
        this.message = builder.message;
        this.messageClass = builder.messageClass;
        this.status = builder.status;
        this.retryCount = builder.retryCount;
    }

    private String id;

    private Object message;

    private String messageClass;

    private SimpleMessageStatus status;

    private int retryCount;

    private Date createdDate;

    private Date updatedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public SimpleMessageStatus getStatus() {
        return status;
    }

    public void setStatus(SimpleMessageStatus status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    public static class SimpleMessageBuilder {

        private String id;
        private Object message;
        private String messageClass;
        private SimpleMessageStatus status;
        private int retryCount;

        public SimpleMessageBuilder() {

        }

        public SimpleMessageBuilder id(String id) {
            this.id = id;
            return this;
        }

        public SimpleMessageBuilder message(Object message) {
            this.message = message;
            return this;
        }

        public SimpleMessageBuilder messageClass(String messageClass) {
            this.messageClass = messageClass;
            return this;
        }

        public SimpleMessageBuilder status(SimpleMessageStatus status) {
            this.status = status;
            return this;
        }

        public SimpleMessageBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public SimpleMessage build() {
            SimpleMessage simpleMessage =  new SimpleMessage(this);
            return simpleMessage;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleMessage that = (SimpleMessage) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}