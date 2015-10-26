package com.yahoo.inmind.comm.news.model;

import com.yahoo.inmind.comm.generic.model.BaseEvent;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;

/**
 * Created by oscarr on 12/22/14.
 * This event notify the subscriber when the list of news articles has changed.
 */
public class NewsUpdateEvent extends BaseEvent {
    private NewsArticleVector news;

    public NewsUpdateEvent(NewsArticleVector news) {
        this.news = news;
    }

    public NewsArticleVector getNews() {
        return news;
    }

    public void setNews(NewsArticleVector news) {
        this.news = news;
    }
}
