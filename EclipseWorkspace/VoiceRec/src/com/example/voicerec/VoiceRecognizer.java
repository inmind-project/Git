package com.example.voicerec;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class VoiceRecognizer extends Activity implements OnClickListener {
	
	protected static final int REQUEST_OK = 1;
	private static final String TAG = "VoiceRecActivity";
	
	private TextView mText;	
	private SpeechRecognizer sr;
	//Android does not allow two intents to access the microphone :(
	//AudioRecord recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognizer);
        findViewById(R.id.button1).setOnClickListener(this);
        
        sr = SpeechRecognizer.createSpeechRecognizer(this);       
        sr.setRecognitionListener(new listener());
        mText = ((TextView)findViewById(R.id.text1));
        
        getApplicationContext();
    }

    class listener implements RecognitionListener          
    {
             public void onReadyForSpeech(Bundle params)
             {
            	    // this methods called when Speech Recognition is ready
            	    // also this is the right time to un-mute system volume because the annoying sound played already
                      Log.d(TAG, "onReadyForSpeech");
             }
             public void onBeginningOfSpeech()
             {
                      Log.d(TAG, "onBeginningOfSpeech");
             }
             public void onRmsChanged(float rmsdB)
             {
                      Log.d(TAG, "onRmsChanged");
             }
             public void onBufferReceived(byte[] buffer)
             {
                      Log.d(TAG, "onBufferReceived:" + buffer.length);
             }
             public void onEndOfSpeech()
             {
                      Log.d(TAG, "onEndofSpeech");
                      //if (recorder != null)
                       	//recorder.release();
             }
             public void onError(int error)
             {
                      Log.d(TAG,  "error: " +  error);
                      mText.setText("error: " + error);
                      
                      //onClick(null);
             }
             public void onResults(Bundle results)                   
             {
                      String str = new String();
                      Log.d(TAG, "onResults " + results);
                      ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                      for (int i = 0; i < data.size(); i++)
                      {
                                Log.d(TAG, "result " + data.get(i));
                                str += data.get(i);
                      }
                      //mText.setText("results: "+String.valueOf(data.size()));
                      String firstRes =data.get(0).toString(); 
                      mText.setText(firstRes);
                      
                      if (firstRes.contains("mind agent"))
                      {
      					Log.d("Main", "Playing notification");
    					Uri notification = RingtoneManager
    							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    					Ringtone r = RingtoneManager.getRingtone(
    							getApplicationContext(), notification);
    					r.play();
                      }
                      
                      
                      //onClick(null);
             }
             public void onPartialResults(Bundle partialResults)
             {
                      Log.d(TAG, "onPartialResults");
             }
             public void onEvent(int eventType, Bundle params)
             {
                      Log.d(TAG, "onEvent " + eventType);
             }
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.voice_recognizer, menu);
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


    @Override
    public void onClick(View v) {
    	//Android does not allow two intents to access the microphone :(
    	//if (recorder == null)
    		//recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,44100,2,AudioFormat.ENCODING_PCM_16BIT,500000);
        Log.d("VS", "Recorder initialized");

        //recorder.startRecording();
        
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
             intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
             intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
             intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

             intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
             
             sr.startListening(intent);
             

//            	 try {
//                 startActivityForResult(i, REQUEST_OK);
//             } catch (Exception e) {
//            	 	Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
//             }
    }
    
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            super.onActivityResult(requestCode, resultCode, data);
//            if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
//            		ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//            		mText.setText(thingsYouSaid.get(0));
//            }
//        }
    
}
