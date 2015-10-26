package com.yahoo.inmind.services.news.view.reader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import com.yahoo.inmind.middleware.R;
import com.yahoo.inmind.services.news.view.i13n.I13NActivity;

@SuppressLint("NewApi")
public class BackableActivity extends I13NActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);	
		//Disable the back button in the action bar
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}
	
	//Enable the back button in the action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{       
		if(((String) menuItem.getTitle()).equals(getResources().getString(R.string.news_name)))
			onBackPressed();
	    return true;
	}
}
