package com.yahoo.inmind.your_app.view;


import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.yahoo.inmind.middleware.events.news.NewsUpdateEvent;
import com.yahoo.inmind.model.NewsArticle;
import com.yahoo.inmind.model.NewsArticleVector;
import com.yahoo.inmind.reader.ReaderMainActivity;
import com.yahoo.inmind.your_app.control.SingletonApp;

public class NewsExtendedActivity extends ReaderMainActivity{

    // ****************************** SERVICE'S LIFECYCLE *******************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if you want to receive asynchronous updates (such as news updates, etc)
        // subscribe to the Message Broker event bus.
        SingletonApp.mMB.subscribe( this );
    }

    /**
     * if you want to receive asynchronous updates (such as news updates, etc)
     * subscribe to the Message Broker event bus.
     */
    @Override
    public void onResume() {
        super.onResume();
        SingletonApp.mMB.subscribe( this );
    }

    /**
     * If you don't want to continue receiving messages from the message broker
     * when the activity is inactive, just unsubscribe from the MB.
     */
    @Override
    public void onPause() {
        super.onPause();
        SingletonApp.mMB.unSubscribe( this );
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SingletonApp.mMB.unSubscribe(this);
    }


    // ****************************** EVENT HANDLERS *******************

    /**
     * This method receives an update of the list of news articles each time it changes.
     * In order to receive this update you have to subscribe to the MB first (see onCreate above)
     * @param event
     */
    public void onEvent( NewsUpdateEvent event ){
        NewsArticleVector list = event.getNews();
        for ( NewsArticle i : list ){
            Log.e("Update", "NewsArticle: " + i.getIdx() + "  " + i.getTitle() );
        }
    }


    // ****************************** AUTO-GENERATED *******************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        return super.onOptionsItemSelected(item);
    }
}
