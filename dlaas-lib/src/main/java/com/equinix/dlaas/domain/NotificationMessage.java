package com.equinix.dlaas.domain;

import java.io.Serializable;

/**
 * Created by ransay on 3/30/2017.
 */
public class NotificationMessage implements Serializable {

    private String networkId;

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

}
