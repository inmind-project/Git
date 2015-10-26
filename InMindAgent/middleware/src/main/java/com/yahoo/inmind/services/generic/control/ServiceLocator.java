package com.yahoo.inmind.services.generic.control;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yahoo.inmind.comm.generic.control.MessageBroker;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.rules.model.DecisionRule;
import com.yahoo.inmind.effectors.alarm.control.AlarmEffector;
import com.yahoo.inmind.effectors.generic.control.EffectorDataReceiver;
import com.yahoo.inmind.effectors.generic.control.EffectorObserver;
import com.yahoo.inmind.effectors.sms.control.SmsEffector;
import com.yahoo.inmind.sensors.accelerometer.control.AccelerometerObserver;
import com.yahoo.inmind.sensors.generic.control.SensorDataReceiver;
import com.yahoo.inmind.sensors.generic.control.SensorObserver;
import com.yahoo.inmind.services.activity.control.ActivityRecognitionService;
import com.yahoo.inmind.services.booking.control.HotelReservationService;
import com.yahoo.inmind.services.calendar.control.CalendarService;
import com.yahoo.inmind.services.location.control.LocationService;
import com.yahoo.inmind.services.news.control.NewsService;
import com.yahoo.inmind.services.streaming.control.StreamingService;
import com.yahoo.inmind.services.weather.control.WeatherService;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * The service locator pattern is a design pattern used in software development to encapsulate the
 * processes involved in obtaining a service with a strong abstraction layer. This pattern uses a
 * central registry known as the "service locator", which on request returns the information
 * necessary to perform a certain task.
 * @see @link https://msdn.microsoft.com/en-us/library/ff648968.aspx
 *
 * Created by oscarr on 8/10/15.
 */
public class ServiceLocator {

    private ConcurrentHashMap<Class, MiddServiceConnection> mServicesHash;
    private ConcurrentHashMap<Class, SensorObserver>  mSensorsHash;
    private ConcurrentHashMap<Class, EffectorObserver> mEffectorsHash;
    private ConcurrentHashMap<Integer, DecisionRule> mDecisionRulesHash;
    private SensorDataReceiver sensorDataReceiver;
    private ServiceDataReceiver serviceDataReceiver;
    private EffectorDataReceiver effectorDataReceiver;
    private ArrayList<String> actionsServiceReceiver;
    private ArrayList<String> actionsSensorReceiver;
    private ArrayList<String> actionsEffectorReceiver;
    private static ServiceLocator serviceLocator;
    private static Context mContext;
    /** Tracking list of started activities **/
    private ArrayList<Class> mClassActivitiesList = new ArrayList<>();
    private ArrayList<Activity> mActivitiesList = new ArrayList<>();

    private ServiceLocator(Context context) {
        mServicesHash = new ConcurrentHashMap<>();
        mSensorsHash = new ConcurrentHashMap<>();
        mEffectorsHash = new ConcurrentHashMap<>();
        mDecisionRulesHash = new ConcurrentHashMap<>();
        mContext = context;
        sensorDataReceiver = new SensorDataReceiver(mContext);
        serviceDataReceiver = new ServiceDataReceiver(mContext);
        effectorDataReceiver = new EffectorDataReceiver(mContext);
        actionsServiceReceiver = new ArrayList<>();
        actionsSensorReceiver = new ArrayList<>();
        actionsEffectorReceiver = new ArrayList<>();
    }

    public static ServiceLocator getInstance(Context context) {
        if (serviceLocator == null) {
            serviceLocator = new ServiceLocator( context );
        }
        return serviceLocator;
    }

    @Nullable
    public static ServiceLocator getExistingInstance() {
        return serviceLocator;
    }

    @Nullable
    public <T extends GenericService> T getService( Class<T> service ){
        if( mServicesHash != null ) {
            return (T) mServicesHash.get(service).getmService();
        }
        return null;
    }

    @Nullable
    public  <T extends SensorObserver> T  getSensor( Class<T> sensor ){
        if( mSensorsHash != null ) {
            return (T) mSensorsHash.get(sensor);
        }
        return null;
    }

    @Nullable
    public  <T extends EffectorObserver> T getEffector( Class<T> effector ){
        if( mEffectorsHash != null ) {
            return (T) mEffectorsHash.get(effector);
        }
        return null;
    }

    @Nullable
    public DecisionRule getDecisionRule(int hashCode){
        if( mDecisionRulesHash != null ) {
            return mDecisionRulesHash.get( hashCode );
        }
        return null;
    }

    public void addDecisionRule(DecisionRule decisionRule){
        mDecisionRulesHash.put(decisionRule.hashCode(), decisionRule);
    }

    public boolean removeDecisionRule(DecisionRule decisionRule){
        return mDecisionRulesHash.remove(decisionRule.hashCode()) == null;
    }

    /**
     * We add all the Middleware services here.
     */
    public void addServices(){
//        mServicesHash.put( NewsService.class, new MiddServiceConnection() );
//        mServicesHash.put( AwareServiceWrapper.class, new MiddServiceConnection() );
//        mServicesHash.put( ActivityRecognitionService.class, new MiddServiceConnection() );
//        mServicesHash.put( HotelReservationService.class, new MiddServiceConnection() );
//        mServicesHash.put( LocationService.class, new MiddServiceConnection() );
//        mServicesHash.put( WeatherService.class, new MiddServiceConnection() );
        mServicesHash.put( CalendarService.class, new MiddServiceConnection() );
        mServicesHash.put( StreamingService.class, new MiddServiceConnection() );
    }

    //FIXME
    public void addSensors(){
        mSensorsHash.put( AccelerometerObserver.class, new AccelerometerObserver( new Handler(), mContext ) );
        //... add all the sensors here

        for(SensorObserver observer : mSensorsHash.values() ){
            registerSensor(observer);
            //observer.startListening();
        }
    }

    public void addEffectors(){
        mEffectorsHash.put( AlarmEffector.class, new AlarmEffector( new Handler(), mContext ) );
        mEffectorsHash.put( SmsEffector.class, new SmsEffector( new Handler(), mContext ) );
        //... add all the effectors here

        for(EffectorObserver observer : mEffectorsHash.values() ){
            registerEffector(observer);
        }
    }

    //TODO: finish it
    public void startSensor(String name){
        if( name.equals(Constants.SENSOR_ACCELEROMETER )){
            getSensor( AccelerometerObserver.class ).startListening();
        }
    }

    //TODO: finish it
    public void stopSensor(String name){
        if( name.equals(Constants.SENSOR_ACCELEROMETER )){
            getSensor( AccelerometerObserver.class ).stopListening();
        }
    }


    private void registerSensor(SensorObserver sensorObserver){
        sensorObserver.unregister(sensorDataReceiver);
        ArrayList<String> actions = sensorObserver.getActions();
        for(String action : actions ){
            if( !actionsSensorReceiver.contains(action) ){
                actionsSensorReceiver.add(action);
            }
        }
        sensorObserver.register( sensorDataReceiver, actionsSensorReceiver );
    }

    private void registerEffector(EffectorObserver effectorObserver){
        effectorObserver.unregister( effectorDataReceiver );
        ArrayList<String> actions = effectorObserver.getActions();
        for(String action : actions ){
            if( !actionsEffectorReceiver.contains(action) ){
                actionsEffectorReceiver.add(action);
            }
        }
        effectorObserver.register( effectorDataReceiver, actionsEffectorReceiver );
    }

    private void registerService(GenericService service){
        ArrayList<String> actions = service.getActions();
        if( !actions.isEmpty() ) {
            service.unregister(serviceDataReceiver);
            for (String action : actions) {
                if (!actionsServiceReceiver.contains(action)) {
                    actionsServiceReceiver.add(action);
                }
            }
            service.register(serviceDataReceiver, actionsServiceReceiver);
        }
    }

    public void bindServices(){
        for(final Class service : mServicesHash.keySet()) {
            try {
                Intent intentService = new Intent(mContext, service);
                mContext.startService(intentService);
                MiddServiceConnection connection = mServicesHash.get(service);
                mContext.bindService(intentService,
                        connection,
                        Context.BIND_AUTO_CREATE);
                mServicesHash.put( service.getClass(), connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void destroy(){
        for( MiddServiceConnection conn : mServicesHash.values() ){
            if( conn != null && conn.mService != null ) {
                conn.mService.onDestroy();
                // Unbind from the service
                if (conn.mServiceBound) {
                    mContext.unbindService(conn);
                    conn.mServiceBound = false;
                }
                conn.mService = null;
            }
        }
        for( SensorObserver sensorObserver : mSensorsHash.values() ){
            sensorObserver.stopListening();
            sensorObserver.unregister();
        }
        for( EffectorObserver effectorObserver : mEffectorsHash.values() ){
            effectorObserver.unregister();
        }
        mDecisionRulesHash = null;
        mServicesHash = null;
        mSensorsHash = null;
        mEffectorsHash = null;
        mContext = null;
        sensorDataReceiver = null;
        serviceDataReceiver = null;
        actionsSensorReceiver = null;
        actionsServiceReceiver = null;
        mClassActivitiesList = null;
        mActivitiesList = null;
        GenericService.release();
        System.gc();
    }

    public void addActivity(Object subscriber) {
        if( subscriber instanceof Activity){
            mActivitiesList.add( (Activity) subscriber );
        }
    }

    public void addActivityClass(Class clazz) {
        mClassActivitiesList.add( clazz );
    }

    @Nullable
    public Activity getTopActivity(){
        if( !mActivitiesList.isEmpty() ){
            return mActivitiesList.get( mActivitiesList.size() - 1 );
        }
        return null;
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public static class MiddServiceBinder extends Binder {
        private GenericService mService;

        public void setService(GenericService mService) {
            this.mService = mService;
        }

        public GenericService getService() {
            // Return this instance of Service so clients can call public methods
            return this.mService;
        }
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private static class MiddServiceConnection implements ServiceConnection {
        private MiddServiceBinder mBinder;
        private Boolean mServiceBound;
        private GenericService mService;

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to Service, cast the IBinder and get Service instance
            mBinder = (MiddServiceBinder) service;
            mService = mBinder.getService();
            mService.doAfterBind();
            serviceLocator.registerService( mService );
            mServiceBound = true;
            MessageBroker.getInstance( mContext ).processRequests();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServiceBound = false;
        }


        public GenericService getmService() {
            return mService;
        }
    }
}
