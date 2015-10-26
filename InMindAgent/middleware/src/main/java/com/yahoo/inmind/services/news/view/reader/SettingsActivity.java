package com.yahoo.inmind.services.news.view.reader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.yahoo.inmind.middleware.R;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.model.i13n.Event;
import com.yahoo.inmind.services.news.control.i13n.I13N;

public class SettingsActivity extends ActionBarActivity { //PreferencesActivity
	boolean bI13nOriginallyEnabled = false;
	private I13N mI13n;
	private String pkgName;
	private Event mEvt;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mI13n = I13N.get();
		instrument();
		
        getFragmentManager().beginTransaction().replace( android.R.id.content, new ReaderPreferenceFragment()).commit();
        ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		bI13nOriginallyEnabled = ReaderController.getInstance().getSettings().getI13NEnabled();
    }

	private void instrument()
	{
		pkgName = this.getClass().getSimpleName();
		mEvt = new Event(pkgName, "");
	}
	
    @SuppressLint("NewApi")
	public static class ReaderPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }
    }
    
    //Enable the back button in the action bar
  	@Override
  	public boolean onOptionsItemSelected(MenuItem menuItem)
  	{       
  		if(((String) menuItem.getTitle()).equals(getResources().getString(R.string.news_name)))
  			onBackPressed();
  	    return true;
  	}

	@Override
	protected void onDestroy()
	{
		if (bI13nOriginallyEnabled != ReaderController.getInstance().getSettings().getI13NEnabled())
		{
			if (ReaderController.getInstance().getSettings().getI13NEnabled())
			{
				I13N.get().log(mEvt.setAction("I13N enabled"));
			}
			else
			{
				ReaderController.getInstance().getSettings().setI13NEnabled(true);
				I13N.get().logImmediately(mEvt.setAction("I13N disabled"));
				ReaderController.getInstance().getSettings().setI13NEnabled(false);
			}
		}
		super.onDestroy();
	}
  	
	@Override
	protected void onResume() {
		super.onResume();
		if (mI13n != null)
		{
			I13N.get().log(new Event(mEvt.setAction("onResume")));
			I13N.get().cancelFlushDelayed();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mI13n != null)
		{
			I13N.get().log(new Event(mEvt.setAction("onPause")));
			I13N.get().flushDelayed();
		}
	}

	public Event getEvent() {
		return mEvt;
	}

	public void setEvent(Event evt) {
		this.mEvt = evt;
	}	
}
