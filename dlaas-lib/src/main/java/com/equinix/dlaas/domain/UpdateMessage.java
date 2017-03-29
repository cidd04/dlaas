package com.equinix.dlaas.domain;

import java.util.List;

/**
 * Created by ransay on 3/28/2017.
 */
public class UpdateMessage {

    private String networkId;
    private List<String> payload;

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public List<String> getPayload() {
        return payload;
    }

    public void setPayload(List<String> payload) {
        this.payload = payload;
    }
}
