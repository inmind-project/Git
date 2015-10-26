package com.yahoo.inmind.comm.battery.model;

import com.yahoo.inmind.comm.generic.model.BaseEvent;

/**
 * Created by oscarr on 8/13/15.
 */
public class BatterySensorEvent extends BaseEvent {
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
