package com.yahoo.inmind.your_app.control;

import android.content.Context;

import com.yahoo.inmind.middleware.control.MessageBroker;

/**
 * Created by oscarr on 12/3/14.
 */
public class SingletonApp {

    private static SingletonApp instance;
    public static MessageBroker mMB;
    // add your global objects here...

    private SingletonApp( Context context ) {
        mMB = MessageBroker.getInstance( context );
    }


    public static SingletonApp getInstance(Context context) {
        if (instance == null) {
            instance = new SingletonApp( context );
        }
        return instance;
    }
}
