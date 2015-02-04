package com.yahoo.inmind.your_app.view;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.yahoo.inmind.middleware.control.MessageBroker;
import com.yahoo.inmind.model.Profile;
import com.yahoo.inmind.model.slingstone.UserProfile;
import com.yahoo.inmind.util.Constants;
import com.yahoo.inmind.your_app.R;
import com.yahoo.inmind.your_app.adapter.ListAdapter;

import java.util.ArrayList;
import java.util.Map;

public class UserProfileList extends ListActivity {

    ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list_layout);

        MessageBroker mb = MessageBroker.getInstance( getApplicationContext() );
        UserProfile userProfile = (UserProfile)
                mb.getObjFromCache( (String) getIntent().getExtras().get( Constants.CONTENT), true );
        items = new ArrayList<>();
        items.add( "User Age:  "+ userProfile.get(Constants.JSON_USER_AGE) );
        items.add( "User Gender:  "+ userProfile.get(Constants.JSON_USER_GENDER) );
        fillProfile( userProfile.getmCapWiki(), "CapWiki" );
        fillProfile( userProfile.getmCapYct(), "CapYct" );
        fillProfile( userProfile.getmFbWiki(), "FbWiki" );
        fillProfile( userProfile.getmFbYct(), "FbYct" );
        fillProfile( userProfile.getmNegDecWiki(), "NegDecWiki" );
        fillProfile( userProfile.getmNegDecYct(), "NegDecYct");
        fillProfile( userProfile.getmNegInfWiki(), "NegInfWiki" );
        fillProfile( userProfile.getmNegInfYct(), "NegInfTct" );
        fillProfile( userProfile.getmPosDecWiki(), "PosDecWiki" );
        fillProfile( userProfile.getmPosDecYct(), "PosDecYct" );
        fillProfile( userProfile.getmUserProp(), "UserProp" );
        setListAdapter( new ListAdapter(this, R.layout.app_item_layout, items) );
    }


    protected void fillProfile(Profile prof, String category) {
        if( prof != null ) {
            for (Map.Entry<String, String> ent : prof.entrySet()) {
                items.add( "["+category+"]  "+ent.getKey() + ":  " + String.valueOf(ent.getValue()) );
            }
        }
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
