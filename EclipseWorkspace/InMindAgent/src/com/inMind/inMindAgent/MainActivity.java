package com.inMind.inMindAgent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

	TTScontroller ttsCont;
	ServerConnector serverConnector;
	
	private Button startButton,stopButton;


	private Handler toasterHandler, talkHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		toasterHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if(msg.arg1==1)
				{
					Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();        
				}
				return false;
			}
		});
		
		talkHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if(msg.arg1==1)
				{
					ttsCont.speakThis(msg.obj.toString());        
				}
				return false;
			}
		});

		ttsCont = new TTScontroller(getApplicationContext());
		serverConnector = new ServerConnector(toasterHandler, talkHandler);

		startButton = (Button) findViewById(R.id.button_rec);
		stopButton = (Button) findViewById(R.id.button_stop);

		startButton.setOnClickListener(startListener);
		stopButton.setOnClickListener(stopListener);

		//minBufSize += 2048;
		//System.out.println("minBufSize: " + minBufSize);

		//attach a Message. set msg.arg to 1 and msg.obj to string for toast.

	}


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

	/** Called when the user clicks the talk button */
	public void talkText(View view) {
		// Do something in response to button
		EditText editText = (EditText) findViewById(R.id.text_to_talk);
		String toSay = editText.getText().toString();
		ttsCont.speakThis(toSay);
	}





	private final OnClickListener stopListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Main", "Stop Clicked");
			//audioStreamer.stopStreaming();
			serverConnector.stopStreaming();
		}

	};

	private final OnClickListener startListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Main", "Start Clicked");
			//audioStreamer.startStreaming();
			serverConnector.ConnectToServer();
		}

	};

	
}
