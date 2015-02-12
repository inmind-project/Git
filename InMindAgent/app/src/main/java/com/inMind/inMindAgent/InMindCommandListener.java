package com.inMind.inMindAgent;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.inMind.pocketSphinx.PocketSphinxSearcher;

public class InMindCommandListener
{

    interface InmindCommandInterface
    {
        void commandDetected();
    }

    InmindCommandInterface minmindCommandInterface;
    Context context;
    PocketSphinxSearcher pocketSphinxSearcher = null;

    InMindCommandListener(InmindCommandInterface inmindCommandInterface, final Context context)
    {
        minmindCommandInterface = inmindCommandInterface;
        this.context = context;

        pocketSphinxSearcher = new PocketSphinxSearcher(context,"in mind agent",new PocketSphinxSearcher.SphinxRes(){

            int i =0;
            @Override
            public void keyDetected() {
                minmindCommandInterface.commandDetected();
            }});
    }

    public void stopListening()
    {
        pocketSphinxSearcher.stopListening();
    }

    public void listenForInmindCommand()
    {
        pocketSphinxSearcher.startListeningForKeyword();
    }
}
