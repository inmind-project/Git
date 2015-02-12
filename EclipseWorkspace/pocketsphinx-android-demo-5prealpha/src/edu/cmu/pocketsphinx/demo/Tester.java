package edu.cmu.pocketsphinx.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Tester extends Activity 
{
	PocketSphinxSearcher pocketSphinxSearcher = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pocketSphinxSearcher = new PocketSphinxSearcher(getApplicationContext(),"what do you want",new PocketSphinxSearcher.SphinxRes(){

        	int i =0;
			@Override
			public void keyDetected() {
//				Uri notification = RingtoneManager
//						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//				Ringtone r = RingtoneManager.getRingtone(
//						getApplicationContext(), notification);
//				r.play();
				i++;
				((TextView) findViewById(R.id.result_text)).setText("sound!" + i);
				
				pocketSphinxSearcher.startListeningForKeyword();
				
			}});
        pocketSphinxSearcher.startListeningForKeyword();
    }
}
