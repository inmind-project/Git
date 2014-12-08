package com.inMind.inMindAgent;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public class TTScontroller {
	
	TextToSpeech ttobj;
	Context appContect;
	
	// should be constructed in OnCreate
	public TTScontroller(Context appContext)
	{
		this.appContect = appContext;
	      ttobj=new TextToSpeech(appContext, 
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
	}
	
    @SuppressWarnings("deprecation") public void speakThis(String message) {
		Toast.makeText(appContect, message, Toast.LENGTH_SHORT).show();
		 ttobj.speak(message, TextToSpeech.QUEUE_FLUSH, null);
	}

}
