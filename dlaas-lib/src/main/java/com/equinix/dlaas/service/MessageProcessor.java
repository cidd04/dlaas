package com.equinix.dlaas.service;

import com.equinix.dlaas.domain.SimpleMessage;

/**
 * Created by ransay on 3/25/2017.
 */
public interface MessageProcessor {

    public void processAsync(SimpleMessage simpleMessage);

}
