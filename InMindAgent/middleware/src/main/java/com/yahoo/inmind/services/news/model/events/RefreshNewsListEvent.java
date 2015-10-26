package com.yahoo.inmind.services.news.model.events;

import com.yahoo.inmind.comm.generic.model.BaseEvent;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;

/**
 * Created by oscarr on 3/10/15.
 */
public class RefreshNewsListEvent extends BaseEvent {
    private NewsArticleVector articleList;

    public NewsArticleVector getArticleList() {
        return articleList;
    }

    public void setArticleList(NewsArticleVector articleList) {
        this.articleList = articleList;
    }
}
