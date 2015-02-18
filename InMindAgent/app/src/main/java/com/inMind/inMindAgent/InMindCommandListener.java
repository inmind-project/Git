package com.inMind.inMindAgent;

import android.content.Context;

import com.inMind.pocketSphinxBridge.PocketSphinxSearcher;

public class InMindCommandListener
{

    interface InmindCommandInterface
    {
        void commandDetected();
    }

    InmindCommandInterface minmindCommandInterface;
    Context context;
    PocketSphinxSearcher pocketSphinxSearcher = null;
    boolean isListeningForCommand = false;

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
        isListeningForCommand = false;
    }

    public void listenForInmindCommand()
    {
        if (!isListeningForCommand)
        {
            pocketSphinxSearcher.startListeningForKeyword();
            isListeningForCommand = true;
        }
    }
}
