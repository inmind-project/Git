package com.yahoo.inmind.services.news.model.events;

import com.yahoo.inmind.comm.generic.model.BaseEvent;

/**
 * Created by oscarr on 2/3/15.
 */
public class ExpandArticleEvent extends BaseEvent {
    private int idx;

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}
