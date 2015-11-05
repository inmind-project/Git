package com.yahoo.inmind.effectors.generic.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.effectors.alarm.control.AlarmEffector;
import com.yahoo.inmind.services.generic.control.ResourceLocator;

/**
 * Created by oscarr on 9/29/15.
 */
public class EffectorDataReceiver extends BroadcastReceiver {
    private MessageBroker mb;
    private Context mContext;
    private ResourceLocator resourceLocator;

    public EffectorDataReceiver(Context context) {
        mContext = context;
    }

    // This is required by the AndroidManifest
    public EffectorDataReceiver() {}

    private void initialize( Context context ){
        if( mb == null ){
            mb = MessageBroker.getInstance( context );
        }
        if( resourceLocator == null ){
            resourceLocator = ResourceLocator.getInstance(context);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        initialize( context );
        if (intent.getAction().equals( Constants.ACTION_SET_ALARM )) {
            resourceLocator.lookupEffector( AlarmEffector.class ).setAlarm( intent );
        }
    }
}
