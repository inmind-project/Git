package com.yahoo.inmind.services.generic.control;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.comm.generic.model.MBRequest;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oscarr on 8/5/15.
 */
public abstract class GenericService extends IntentService{
    protected static Context mContext;
    protected static ServiceLocator serviceLocator;
    protected String mAwarePackageName;
    protected HashMap<Integer, MBRequest> requests;
    protected ArrayList<BroadcastReceiver> receivers;
    protected String status;
    protected ArrayList<String> actions;

    protected static final String STARTED = "STARTED";
    protected static final String STOPPED = "STOPPED";

    /** Communication **/
    protected static MessageBroker mb;

    /** Binder given to clients */
    protected IBinder mBinder = new ServiceLocator.MiddServiceBinder();



    public GenericService(){
        this("default", null);
    }

    public GenericService(String name, String awarePackageName){
        super(name);
        mb = MessageBroker.getInstance( null );
        mContext = MessageBroker.getContext();
        mAwarePackageName = awarePackageName;
        receivers = new ArrayList<>();
        status = STOPPED;
        serviceLocator = ServiceLocator.getInstance( mContext );
        actions = new ArrayList<>();
    }

    public HashMap<Integer, MBRequest> getRequests() {
        return requests;
    }


    public void start() {
        if( mAwarePackageName != null ) {
            AwareServiceWrapper.startPlugin(mContext, mAwarePackageName);
            status = STARTED;
        }
    }

    public void stop() {
        if( mAwarePackageName != null ) {
            AwareServiceWrapper.stopPlugin(mContext, mAwarePackageName);
            status = STOPPED;
        }
        stopSelf();
    }

    public GenericService register(BroadcastReceiver receiver, ArrayList<String> actions) {
        IntentFilter contextFilter = new IntentFilter();
        for (String action : actions) {
            contextFilter.addAction(action);
        }
        mContext.registerReceiver(receiver, contextFilter);
        if( !receivers.contains( receiver ) ) {
            receivers.add(receiver);
        }
        return this;
    }

    public void unregister(BroadcastReceiver receiver) {
        try {
            receivers.remove(receiver);
            mContext.unregisterReceiver(receiver);
        }catch (IllegalArgumentException e) {
            //do nothing
        }
    }

    public void unregister(){
        if( receivers != null ) {
            for (BroadcastReceiver receiver : receivers) {
                unregister(receiver);
            }
            receivers = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if( mContext == null ) {
            mContext = getApplicationContext();
        }
        mb = MessageBroker.getInstance(mContext);
        mb.subscribe(this); //subscribe();
        ((ServiceLocator.MiddServiceBinder) mBinder).setService(this);
        start();
        return mBinder;
    }

//    private void subscribe(){
//        Method[] methods = this.getClass().getMethods();
//        for( Method method : methods ){
//            if( method.getName().startsWith("onEvent") ){
//                mb.subscribe(this);
//            }
//        }
//    }


    public abstract void doAfterBind();


    @Override
    public void onDestroy() {
        unregister();
        super.onDestroy();
        stop();
        requests = null;
        receivers = null;
        actions = null;
        System.gc();
    }

    public static void release(){
        mContext = null;
        serviceLocator = null;
        mb = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //do nothing
    }


    public ArrayList<String> getActions() {
        return actions;
    }

    public void setActions(ArrayList<String> actions) {
        this.actions = actions;
    }
}
