package com.yahoo.inmind.services.news.model.events;

import com.yahoo.inmind.comm.generic.model.BaseEvent;

/**
 * Created by oscarr on 12/23/14.
 */
public class ResponseFetchNewsEvent extends BaseEvent {
    public ResponseFetchNewsEvent(Integer mbRequestId) {
        super( mbRequestId );
    }

}
