package com.yahoo.inmind.comm.news.model;

import com.yahoo.inmind.comm.generic.model.BaseEvent;
import com.yahoo.inmind.comm.generic.model.MBRequest;

/**
 * Created by oscarr on 7/1/15.
 */
public class FilterByEmailEvent extends BaseEvent {

    private MBRequest mbRequest;

    public FilterByEmailEvent(MBRequest mbRequest) {
        this.mbRequest = mbRequest;
    }

    public void setMbRequest(MBRequest mbRequest) {
        this.mbRequest = mbRequest;
    }

    public MBRequest getMbRequest() {
        return mbRequest;
    }
}
