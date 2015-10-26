package com.yahoo.inmind.effectors.generic.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.effectors.alarm.control.AlarmEffector;
import com.yahoo.inmind.services.generic.control.ServiceLocator;

/**
 * Created by oscarr on 9/29/15.
 */
public class EffectorDataReceiver extends BroadcastReceiver {
    private MessageBroker mb;
    private Context mContext;
    private ServiceLocator serviceLocator;

    public EffectorDataReceiver(Context context) {
        mContext = context;
    }

    public EffectorDataReceiver() {
        Log.e("", "INSIDE EffectorDataReceiver");
    }

    private void initialize( Context context ){
        if( mb == null ){
            mb = MessageBroker.getInstance( context );
        }
        if( serviceLocator == null ){
            serviceLocator = ServiceLocator.getInstance( context );
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        initialize( context );
        if (intent.getAction().equals( Constants.ACTION_SET_ALARM )) {
            serviceLocator.getEffector( AlarmEffector.class ).setAlarm( intent );
        }
    }
}
