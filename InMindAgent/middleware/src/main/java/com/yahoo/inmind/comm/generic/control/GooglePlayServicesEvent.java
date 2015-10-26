package com.yahoo.inmind.comm.generic.control;

import com.yahoo.inmind.comm.generic.model.BaseEvent;

/**
 * Created by oscarr on 9/14/15.
 */
public class GooglePlayServicesEvent extends BaseEvent {
    private boolean error;
    private String notification;

    private GooglePlayServicesEvent(){ super(); }
    private GooglePlayServicesEvent(int mbRequestId){ super( mbRequestId); }

    public static GooglePlayServicesEvent build(){
        return new GooglePlayServicesEvent();
    }
    public static GooglePlayServicesEvent build(int mbRequestId){
        return new GooglePlayServicesEvent(mbRequestId);
    }

    public String getNotification() {
        return notification;
    }

    public GooglePlayServicesEvent setNotification(String notification) {
        this.notification = notification;
        return this;
    }

    public boolean isError() {
        return error;
    }

    public GooglePlayServicesEvent setError(boolean error) {
        this.error = error;
        return this;
    }
}
