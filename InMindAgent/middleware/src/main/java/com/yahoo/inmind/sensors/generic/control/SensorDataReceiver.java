package com.yahoo.inmind.sensors.generic.control;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.aware.Accelerometer;
import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.sensors.accelerometer.control.AccelerometerObserver;
import com.yahoo.inmind.services.generic.control.ServiceLocator;

/**
 * Created by oscarr on 8/13/15.
 */
public class SensorDataReceiver extends BroadcastReceiver {
    private MessageBroker mb;
    private Context mContext;
    private ServiceLocator serviceLocator;

    public SensorDataReceiver(Context context){
        mContext = context;
    }

    private void initialize( Context context ){
        if( mb == null ){
            mb = MessageBroker.getInstance( context );
        }
        if( serviceLocator == null ){
            serviceLocator = ServiceLocator.getInstance(context);
        }
    }

    //TODO: finish it
    @Override
    public void onReceive(Context context, Intent intent) {
        initialize( context );
        if( intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)) {
            ContentValues data = intent.getParcelableExtra(Accelerometer.EXTRA_DATA);
            serviceLocator.getSensor(AccelerometerObserver.class ).extractData( data );
        }
    }
}
