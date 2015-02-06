package com.example.sandbox;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;



public class MainActivity extends Activity {
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button startButton = (Button) findViewById(R.id.btn_start);
        Button stopButton = (Button) findViewById(R.id.btn_stop);

		startButton.setOnClickListener(startListener);
		stopButton.setOnClickListener(stopListener);

    }
    
	private final OnClickListener stopListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Main", "Stop Clicked");

		}

	};

	private final OnClickListener startListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Main", "Start Clicked");

		}

	};


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
