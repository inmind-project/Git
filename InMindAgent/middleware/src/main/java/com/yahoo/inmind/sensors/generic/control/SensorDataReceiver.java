package com.yahoo.inmind.sensors.generic.control;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.aware.Accelerometer;
import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.sensors.accelerometer.control.AccelerometerObserver;
import com.yahoo.inmind.services.generic.control.ResourceLocator;

/**
 * Created by oscarr on 8/13/15.
 */
public class SensorDataReceiver extends BroadcastReceiver {
    private MessageBroker mb;
    private Context mContext;
    private ResourceLocator resourceLocator;

    // This is required by the AndroidManifest
    public SensorDataReceiver(){}

    public SensorDataReceiver(Context context){
        mContext = context;
    }

    private void initialize( Context context ){
        if( mb == null ){
            mb = MessageBroker.getInstance( context );
        }
        if( resourceLocator == null ){
            resourceLocator = ResourceLocator.getInstance(context);
        }
    }

    //TODO: finish it
    @Override
    public void onReceive(Context context, Intent intent) {
        initialize( context );
        if( intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)) {
            ContentValues data = intent.getParcelableExtra(Accelerometer.EXTRA_DATA);
            resourceLocator.lookupSensor(AccelerometerObserver.class ).extractData( data );
        }
    }
}
