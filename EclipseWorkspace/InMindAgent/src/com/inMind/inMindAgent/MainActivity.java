package com.inMind.inMindAgent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import com.inMind.inMindAgent.R;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
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

	TextToSpeech ttobj;
	
	private String ipAddr = "128.2.209.220";
	private Button startButton,stopButton;

	public byte[] buffer;
	public static DatagramSocket socket;
	private int port=50005;
	AudioRecord recorder;

	private int sampleRate = 44100;
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;  
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;       
	private boolean status = true;
	
	private Handler toasterHandler;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
	      ttobj=new TextToSpeech(getApplicationContext(), 
	    	      new TextToSpeech.OnInitListener() {
	    	      @Override
	    	      public void onInit(int status) {
	    	         if(status != TextToSpeech.ERROR){
	    	             ttobj.setLanguage(Locale.US);
	    	             //ttobj.setPitch(3f);
	    	             //ttobj.setSpeechRate(0.1f);
	    	            }				
	    	         }
	          },"edu.cmu.cs.speech.tts.flite");
	      
	      startButton = (Button) findViewById(R.id.button_rec);
	      stopButton = (Button) findViewById(R.id.button_stop);
	
	      startButton.setOnClickListener(startListener);
	      stopButton.setOnClickListener(stopListener);
	
	      //minBufSize += 2048;
	      //System.out.println("minBufSize: " + minBufSize);
	      
          //attach a Message. set msg.arg to 1 and msg.obj to string for toast.
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
    
    /** Called when the user clicks the Send button */
    public void talkText(View view) {
        // Do something in response to button
    	EditText editText = (EditText) findViewById(R.id.text_to_talk);
    	String toSay = editText.getText().toString();
    	speakThis(toSay);
    	
    }
  

    @SuppressWarnings("deprecation") private void speakThis(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
		 ttobj.speak(message, TextToSpeech.QUEUE_FLUSH, null);
	}
    
    
    
    

private final OnClickListener stopListener = new OnClickListener() {

    @Override
    public void onClick(View arg0) {
    			Log.d("VS", "Stop Clicked");
                status = false;
                if (recorder != null)
                	recorder.release();
                Log.d("VS","Recorder released");
    }

};

private final OnClickListener startListener = new OnClickListener() {

    @Override
    public void onClick(View arg0) {
    			Log.d("VS", "Start Clicked");
                status = true;
                startStreaming();           
    }

};

public void startStreaming() {


    Thread streamThread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
            	Log.d("VS", "Before Creating socket");
                socket = new DatagramSocket();
                Log.d("VS", "Socket Created.");
            	int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            	
                Log.d("VS", "minBufSize:" + minBufSize);
                byte[] buffer = new byte[minBufSize];

                Log.d("VS","Buffer created of size " + minBufSize);
                DatagramPacket packet;

                final InetAddress destination = InetAddress.getByName(ipAddr);
                Log.d("VS", "Address retrieved");


                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                Log.d("VS", "Recorder initialized");

                recorder.startRecording();
                
                //Added to remove initial noise which Android (or at least Nexus 5) seems to have at the beginning.
                Message msgWait = new Message();
                msgWait.arg1 = 1;
                msgWait.obj = "Wait!";
                toasterHandler.sendMessage(msgWait);
                
                Thread.sleep(1600);

                Message msgTalk = new Message();
                msgTalk.arg1 = 1;
                msgTalk.obj = "Talk!";
                toasterHandler.sendMessage(msgTalk);
                
                Thread.sleep(300);
                //couldn't find a better way to clear buffer.
                byte[] tmpbuffer = new byte[minBufSize*1000];
                recorder.read(tmpbuffer, 0, minBufSize*1000);

                while(status == true) {


                    //reading data from MIC into buffer
                    minBufSize = recorder.read(buffer, 0, buffer.length);

                    //putting buffer in the packet
                    packet = new DatagramPacket (buffer,buffer.length,destination,port);

                    socket.send(packet);
                    System.out.println("Send_Packet: " +minBufSize);
                }
                
                socket.close();
                Log.d("VS", "Socket Closed");
                

            } catch(UnknownHostException e) {
                Log.e("VS", "UnknownHostException");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("VS", "IOException");
            } catch (InterruptedException e) {
				e.printStackTrace();
				Log.e("VS", "InterruptedException");
			} 

        }

    });
    streamThread.start();
 }
    
}
