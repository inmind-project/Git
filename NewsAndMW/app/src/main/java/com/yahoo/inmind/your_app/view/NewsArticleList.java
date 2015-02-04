package com.yahoo.inmind.your_app.view;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.yahoo.inmind.middleware.control.MessageBroker;
import com.yahoo.inmind.util.Constants;
import com.yahoo.inmind.your_app.R;
import com.yahoo.inmind.your_app.adapter.ListAdapter;

import java.util.ArrayList;

public class NewsArticleList extends ListActivity {

    private MessageBroker mb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list_layout);

        mb = MessageBroker.getInstance( getApplicationContext() );
        ArrayList<String> items = (ArrayList<String>)
                    mb.getObjFromCache( getIntent().getExtras().get(Constants.CONTENT), false);
        setListAdapter( new ListAdapter(this, R.layout.app_item_layout, items) );
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        // remove the object from cache.
        mb.getObjFromCache( getIntent().getExtras().get(Constants.CONTENT), true);
    }


    // ********************** AUTO-GENERATED *******************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news_summary_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
