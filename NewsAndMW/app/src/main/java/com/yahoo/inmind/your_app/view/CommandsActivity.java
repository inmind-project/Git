package com.yahoo.inmind.your_app.view;


import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;

import com.yahoo.inmind.middleware.control.MessageBroker;
import com.yahoo.inmind.middleware.events.MBRequest;
import com.yahoo.inmind.model.NewsArticleVector;
import com.yahoo.inmind.reader.ReaderMainActivity;
import com.yahoo.inmind.util.Constants;
import com.yahoo.inmind.your_app.control.SingletonApp;

import java.util.ArrayList;
import java.util.Locale;

public class CommandsActivity extends ReaderMainActivity{

    private MessageBroker mMB;
    private static int position = 0;
    private SpeechRecognizer sr;
    private TextToSpeech tts;

    // commands
    private static final String PREVIOUS = "previous";
    private static final String NEXT = "next";
    private static final String EXPAND = "expand";
    private static final String READ = "read";


    // ****************************** SERVICE'S LIFECYCLE ******************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // you can replace this with MessageBroker.getInstance( getApplicationContext() )
        // and will produce the same result
        mMB = SingletonApp.mMB;
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        init();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if( sr != null ){
            sr.destroy();
        }
        if( tts != null ){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }


    private void init(){
        sr.setRecognitionListener( new listener() );
        recognizeSpeech();
        tts = new TextToSpeech( getApplicationContext(), new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                tts.setLanguage( Locale.US );
            }
        });
    }


    // ****************************** COMMANDS *****************************************************

    public void nextArticle(){
        MBRequest request = new MBRequest(Constants.MSG_SHOW_ARTICLE);
        request.put(Constants.BUNDLE_ARTICLE_ID, ++position);
        mMB.send(request);
    }

    public void previousArticle(){
        MBRequest request = new MBRequest(Constants.MSG_SHOW_ARTICLE);
        request.put( Constants.BUNDLE_ARTICLE_ID, --position );
        mMB.send(request);
    }

    public void expandArticle(){
        MBRequest request = new MBRequest(Constants.MSG_EXPAND_ARTICLE);
        request.put( Constants.BUNDLE_ARTICLE_ID, position );
        mMB.send(request);
    }

    public void readArticle(){
        String title = NewsArticleVector.getInstance().get( position ).getTitle();
        String summary = NewsArticleVector.getInstance().get( position ).getSummary();
        tts.speak( title+". "+summary, TextToSpeech.QUEUE_FLUSH, null, String.valueOf( title.hashCode() ));
    }



    // ****************************** SPEECH-TO-TEXT NAD TEXT-TO-SPEECH *****************************


    class listener implements RecognitionListener{
        public void onReadyForSpeech(Bundle params){}
        public void onBeginningOfSpeech(){}
        public void onRmsChanged(float rmsdB){}
        public void onBufferReceived(byte[] buffer){}
        public void onEndOfSpeech(){}
        public void onError(int error){
            recognizeSpeech();}
        public void onResults(Bundle results){
            tts.stop();
            ArrayList commands = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            processCommand( commands );
            recognizeSpeech();
        }
        public void onPartialResults(Bundle partialResults){}
        public void onEvent(int eventType, Bundle params){}
    }

    /**
     * google speech input dialog
     * */
    private void recognizeSpeech() {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        sr.startListening(intent);
    }

    /**
     * Process speech input command
     * */
    private void processCommand( ArrayList<String> commands) {
        for( String command : commands ) {
            if (command.equals( NEXT )) {
                nextArticle();
                return;
            } else if (command.equals( PREVIOUS )) {
                previousArticle();
                return;
            } else if (command.equals( EXPAND )) {
                expandArticle();
                return;
            } else if (command.equals( READ )) {
                readArticle();
                return;
            }
        }
    }



    // ****************************** AUTO-GENERATED ***********************************************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        return super.onOptionsItemSelected(item);
    }
}
