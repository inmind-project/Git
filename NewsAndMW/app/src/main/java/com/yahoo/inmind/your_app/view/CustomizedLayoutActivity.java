package com.yahoo.inmind.your_app.view;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;

import com.yahoo.inmind.events.NewsUIRenderEvent;
import com.yahoo.inmind.middleware.control.MessageBroker;
import com.yahoo.inmind.reader.ReaderMainActivity;
import com.yahoo.inmind.your_app.control.SingletonApp;

public class CustomizedLayoutActivity extends ReaderMainActivity {

    private MessageBroker mMB;

    // ****************************** ACTIVITY LIFECYCLE *******************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMB = SingletonApp.mMB;
        mMB.subscribe( this );
    }

    // ****************************** EVENT HANDLERS *******************

    /**
     * This event handler gets the set of reader's UI widgets that are rendered by the base news
     * activity. You can add extra behavior to these widgets before render action takes place.
     * @param event
     */
    public void onEvent( NewsUIRenderEvent event ){
        // Now, you can modify the UI components' behavior:
        // for instance, let's assume you want to make the summary scrollable and convert the
        // article's title text to uppercase
        if( event.getUiTVSummary() != null ) {
            event.getUiTVSummary().setMovementMethod(new ScrollingMovementMethod());
        }
        if( event.getUiTVTitle() != null ){
            event.getUiTVTitle().setAllCaps(true);
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
        return super.onOptionsItemSelected(item);
    }
}
