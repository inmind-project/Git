package com.inMind.inMindAgent;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	TTScontroller ttsCont;
	LogicController logicController;

	private ImageButton startButton;
	private Button stopButton;

	private Handler userNotifierHandler, talkHandler, launchHandler, ttsCompleteHandler; // TODO: should
																// these all be
																// combined to
																// one handler?
	
	boolean needToReconnect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		userNotifierHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (msg.arg1 == 1) {
					boolean important = msg.arg2 == 1;
					String toToast = msg.obj.toString();
					toastWithTimer(toToast, important);
					if (toToast.equals("Talk!")) //if needs to talk, set recording image. //TODO: should be done nicer (all strings should be refactorred).
					{
						((ImageView)findViewById(R.id.image_recording)).setImageResource(R.drawable.rec_recording);
					}
					else
						((ImageView)findViewById(R.id.image_recording)).setImageResource(R.drawable.not_recording);
				} else if (msg.arg1 == 2) {
					Log.d("Main", "Playing notification");
					Uri notification = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					Ringtone r = RingtoneManager.getRingtone(
							getApplicationContext(), notification);
					r.play();
				}
				else if (msg.arg1 == 0)
					((ImageView)findViewById(R.id.image_recording)).setImageResource(R.drawable.not_recording); //just turn off recording image.
				return false;
			}

		});

		ttsCompleteHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				
				logicController.reconnectIfNeeded();
				return false;
			}
		});
		
		talkHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (msg.arg1 == 1) {
					String toSay = msg.obj.toString();
					ttsCont.speakThis(toSay);
					toastWithTimer(toSay, true);
				}
				return false;
			}
		});

		launchHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (msg.arg1 == 1) {
					// Pattern p = Pattern.compile("(.*)/(.*)");
					// Matcher m = p.matcher(msg.obj.toString());
					// m.find();

					Intent i = getApplicationContext().getPackageManager()
							.getLaunchIntentForPackage(msg.obj.toString());// m.group(1));
					getApplicationContext().startActivity(i);

					// Intent myIntent = new Intent();
					// //myIntent.setClassName("com.android.calculator2","com.android.calculator2.Calculator");
					// myIntent.setClassName(m.group(1),m.group(2));
					// //myIntent.putExtra("com.android.samples.SpecialValue",
					// "Hello, Joe!"); // key/value pair, where key needs
					// current package prefix.
					// startActivity(myIntent);
				}
				return false;
			}
		});
		
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		//intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
		            Log.d("Main Activity", Intent.ACTION_SCREEN_OFF);
		            logicController.closeConnection();
		        }
		    }
		}, intentFilter);

		ttsCont = new TTScontroller(getApplicationContext(),ttsCompleteHandler);
		logicController = new LogicController(userNotifierHandler, talkHandler,
				launchHandler);

		startButton = (ImageButton) findViewById(R.id.button_rec);
		stopButton = (Button) findViewById(R.id.button_stop);

		startButton.setOnClickListener(startListener);
		stopButton.setOnClickListener(stopListener);

		// minBufSize += 2048;
		// System.out.println("minBufSize: " + minBufSize);

		// attach a Message. set msg.arg to 1 and msg.obj to string for toast.

	}

	Date lastToastFinishes = new Date();

	private void toastWithTimer(String toToast, boolean important) {
		// toastCanceller.removeCallbacks(null);//make sure it won't be removed
		// by previous calls
		Date timeNow = new Date();
		boolean isAfter = timeNow.after(lastToastFinishes); //did we already pass the last toast finish time?

		int toastTime = important ? (int) ((toToast.length() / 75.0) * 2500 + 1000)
				: 1000;
		if (toastTime > 3500) // max toast time is 3500...
			toastTime = 3500;
		
		final int toastTimeFinal = toastTime;
		final Toast toast = Toast.makeText(getApplicationContext(), toToast,
				Toast.LENGTH_LONG);



		if (isAfter) {
			toast.show();
			{
				Handler toastCanceller = new Handler();
				toastCanceller.postDelayed(new Runnable() {
					@Override
					public void run() {
						toast.cancel();
					}
				}, toastTimeFinal);
			}
			// set for when this toast will finish
			lastToastFinishes = addMillisec(timeNow, toastTime);
		} else // if not, need to take care of delay for start as well
		{
			int startIn = subtractDatesInMillisec(lastToastFinishes, timeNow);//lastToastFinishes - timeNow;
			Handler toastStarter = new Handler();
			toastStarter.postDelayed(new Runnable() {
				@Override
				public void run() {
					toast.show();
					Handler toastCanceller = new Handler();
					toastCanceller.postDelayed(new Runnable() {
						@Override
						public void run() {
							toast.cancel();
						}
					}, toastTimeFinal);

				}
			}, startIn);
			// set for when this toast will finish
			lastToastFinishes = addMillisec(lastToastFinishes, toastTime);
		}

	}

	static Date addMillisec(Date base, int millisec) {
		return new Date(base.getTime()+millisec);
	}
	
	//returns date1 - date2 in milliseconds
	static int subtractDatesInMillisec(Date date1, Date date2) {
		return (int)(date1.getTime() - date2.getTime());
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
		if (id == R.id.action_toDesk) {
			if (logicController != null)
				logicController.changeInitIpAddr("128.2.213.163");
			return true;
		}
		if (id == R.id.action_toLap) {
			if (logicController != null)
				logicController.changeInitIpAddr("128.2.209.220");
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
		toastWithTimer(toSay, true);
	}

	private final OnClickListener stopListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Main", "Stop Clicked");
			// audioStreamer.stopStreaming();
			logicController.stopStreaming();
		}

	};

	private final OnClickListener startListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d("Main", "Start Clicked");
			// audioStreamer.startStreaming();
			logicController.ConnectToServer();
		}

	};

}
