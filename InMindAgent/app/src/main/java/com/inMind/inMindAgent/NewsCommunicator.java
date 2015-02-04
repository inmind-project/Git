package com.inMind.inMindAgent;

import android.content.Context;
import android.util.Log;

import com.yahoo.inmind.middleware.control.MessageBroker;
import com.yahoo.inmind.middleware.events.MBRequest;
import com.yahoo.inmind.reader.ReaderMainActivity;
import com.yahoo.inmind.util.Constants;

/**
 * Created by Amos Azaria on 03-Feb-15.
 */
public class NewsCommunicator
{
    static int position = 0;


    public static void dealWithMessage(String args, MessageBroker messageBroker)
    {
        if (messageBroker == null)
        {
            Log.e("NewsCummincatior", "Error messageBroker==null");
            return;
        }
        Log.d("Middleware", "Contacting News");

        try
        {
            if (args.equalsIgnoreCase("launch"))
            {
                MBRequest request = new MBRequest(Constants.MSG_LAUNCH_BASE_NEWS_ACTIVITY);
                messageBroker.send(request);
            }
            else if (args.equalsIgnoreCase("next"))
            {
                MBRequest request = new MBRequest(Constants.MSG_SHOW_ARTICLE);
                request.put(Constants.BUNDLE_ARTICLE_ID, ++position);
                messageBroker.send(request);
            }
            else if (args.equalsIgnoreCase("previous"))
            {
                if (position > 0)
                {
                    MBRequest request = new MBRequest(Constants.MSG_SHOW_ARTICLE);
                    request.put(Constants.BUNDLE_ARTICLE_ID, --position);
                    messageBroker.send(request);
                }
            }
            else if (args.equalsIgnoreCase("expand"))
            {
                MBRequest request = new MBRequest(Constants.MSG_EXPAND_ARTICLE);
                request.put(Constants.BUNDLE_ARTICLE_ID, position);
                messageBroker.send(request);
            }
        }
        catch (Exception ex)
        {
            Log.e("Middleware", "Excpetion creating News Activity: " + ex.getMessage());
        }

    }
}
