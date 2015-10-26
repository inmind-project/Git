package com.yahoo.inmind.comm.generic.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import com.aware.Aware_Preferences;
import com.yahoo.inmind.comm.generic.model.BaseEvent;
import com.yahoo.inmind.comm.generic.model.MBRequest;
import com.yahoo.inmind.comm.location.model.LocationEvent;
import com.yahoo.inmind.comm.streaming.model.AudioRecordEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.FileUploader;
import com.yahoo.inmind.commons.control.UtilServiceAPIs;
import com.yahoo.inmind.sensors.accelerometer.control.AccelerometerObserver;
import com.yahoo.inmind.sensors.audio.control.AudioController;
import com.yahoo.inmind.services.activity.control.ActivityRecognitionService;
import com.yahoo.inmind.services.booking.control.HotelReservationService;
import com.yahoo.inmind.services.calendar.control.CalendarService;
import com.yahoo.inmind.services.generic.control.AwareServiceWrapper;
import com.yahoo.inmind.services.generic.control.ServiceLocator;
import com.yahoo.inmind.services.location.control.LocationService;
import com.yahoo.inmind.services.news.control.NewsService;
import com.yahoo.inmind.services.streaming.control.StreamingService;
import com.yahoo.inmind.services.streaming.model.StreamingSubscriptionVO;
import com.yahoo.inmind.services.weather.control.WeatherService;
import com.yahoo.inmind.services.booking.model.HotelSearchCriteria;
import com.yahoo.inmind.services.booking.model.HotelVO;
import com.yahoo.inmind.services.location.model.LocationVO;
import com.yahoo.inmind.services.news.model.events.ResponseFetchNewsEvent;
import com.yahoo.inmind.services.news.model.slingstone.ModelDistribution;
import com.yahoo.inmind.services.news.model.slingstone.UserProfile;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;
import com.yahoo.inmind.services.location.view.LocationSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;


/**`
 * Created by oscarr on 12/5/14.
 */
public class MessageBroker {


    /** Publish/subscribe */
    private static EventBus mEventBus;
    private HashMap<Integer, SubscriberEvent> mSubscribers;

    private static MessageBroker mMB;
    private static Context mContext;
    private static ServiceLocator mServiceLocator;

    /** Cache Memory **/
    private static LruCache<Object, Object> mCache;

    /** Requests for onServiceConnected **/
    private static ArrayList<MBRequest> mRequests = new ArrayList<>();

    // ****************************** MessageBroker's LIFECYCLE ************************************

    private MessageBroker() {
        mEventBus = EventBus.getDefault();
        mCache = new LruCache<>( 500 );
        mSubscribers = new HashMap<>();
    }


    @Nullable
    public static MessageBroker getExistingInstance() {
        return mMB;
    }

    /**
     * Singleton
     * @return
     */
    public static MessageBroker getInstance( Context app ){
        if( mMB == null ){
            if ( app == null ){
                Log.e( "MessageBroker", "There is no Context to bind the component.");
            }else{
                mContext = app;
                mMB = new MessageBroker();
                postCreate();
            }
        }
        return mMB;
    }


    /**
     * We need to execute some functionality outside the MB constructor otherwise
     * we would enter into an endless loop and circular references (e.g., ServiceLocator
     * constructor calls the MB getInstance which in turns call the constructor which
     * in turns calls the ServiceLocator...)
     */
    private static void postCreate(){
        mServiceLocator = ServiceLocator.getInstance( mContext );
        mServiceLocator.addServices();
        mServiceLocator.bindServices();
        mServiceLocator.addSensors();
        mServiceLocator.addEffectors();
    }


    public void destroy(){
        mServiceLocator.destroy();
        mServiceLocator = null;
        mMB = null;
        mContext = null;
        mCache.evictAll();
        mSubscribers = null;
        System.gc();
    }




    // ****************************** HELPER CLASSES **********************************************


    class SubscriberEvent{
        private Class event;
        private Object subscriber;

        public SubscriberEvent( Class event, Object subscriber ){
            this.event = event;
            this.subscriber = subscriber;
        }

        public Class getEvent() {
            return event;
        }

        public void setEvent(Class event) {
            this.event = event;
        }

        public Object getSubscriber() {
            return subscriber;
        }

        public void setSubscriber(Object subscriber) {
            this.subscriber = subscriber;
        }
    }


    // ****************************** REQUEST AND EVENT HANDLERS ***********************************

    /**
     * Use this method to send asynchronous mRequests to the backend (services, background tasks, etc.)
     * If your request returns a result, then it should be handled by an event handler defined outside
     * the Message Broker (usually in the parent class that invoked the message broker's send method)
     * The result of this request is delivered to all the mSubscribers of the resulting event
     * @param request
     */
    public void send(Object request) {
        try {
            if (request instanceof MBRequest) {
                MBRequest mbRequest = (MBRequest) request;
                final int requestId = mbRequest.getRequestId();
                switch (requestId) {
                    case Constants.MSG_LAUNCH_BASE_NEWS_ACTIVITY:
                        launchNewsActivity(mbRequest);
                        break;
                    case Constants.MSG_LAUNCH_EXT_NEWS_ACTIVITY:
                        launchNewsActivity(mbRequest);
                        break;
                    case Constants.MSG_REQUEST_NEWS_ITEMS:
                        requestNewsItems(mbRequest);
                        break;
                    case Constants.MSG_SHOW_MODIFIED_NEWS:
                        launchNewsActivity(mbRequest);
                        break;
                    case Constants.MSG_LOGIN_NEWS:
                        login(mbRequest);
                        break;
                    case Constants.MSG_SHOW_NEWS_ARTICLE:
                        showArticle(mbRequest);
                        break;
                    case Constants.MSG_EXPAND_NEWS_ARTICLE:
                        expandArticle(mbRequest);
                        break;
                    case Constants.MSG_LAUNCH_ACTIVITY:
                        launchActivity(mbRequest);
                        break;
                    case Constants.MSG_SHOW_NEXT_NEWS_ARTICLE:
                        showNextArticle(mbRequest);
                        break;
                    case Constants.MSG_SHOW_CURRENT_NEWS_ARTICLE:
                        showCurrentArticle(mbRequest);
                        break;
                    case Constants.MSG_SHOW_PREVIOUS_NEWS_ARTICLE:
                        showPreviousArticle(mbRequest);
                        break;
                    case Constants.MSG_START_AUDIO_RECORD:
                        recordAudio(mbRequest);
                        break;
                    case Constants.MSG_STOP_AUDIO_RECORD:
                        stopRecordAudio(mbRequest);
                        break;
                    case Constants.MSG_UPLOAD_TO_SERVER:
                        uploadToServer(mbRequest);
                        break;
                    case Constants.MSG_FILTER_NEWS_BY_EMAIL:
                        filterNewsByEmail(mbRequest);
                        break;
                    case Constants.MSG_PROCESS_EVENTS_CALENDAR:
                        processEventsCalendar(mbRequest);
                        break;
                    case Constants.MSG_CHOOSE_ACCOUNT:
                        chooseAccount(mbRequest);
                        break;
                    case Constants.MSG_START_AWARE_PLUGIN:
                        startAwarePlugin(mbRequest);
                        break;
                    case Constants.MSG_STOP_AWARE_PLUGIN:
                        stopAwarePlugin(mbRequest);
                        break;
                    case Constants.MSG_SET_LOCATION:
                        setLocation(mbRequest);
                        break;
                    case Constants.MSG_WEATHER_SUBSCRIBE_TO_FORECAST:
                        subscribeForecast(mbRequest);
                        break;
                    case Constants.MSG_WEATHER_UNSUBSCRIBE_TO_FORECAST:
                        unsubscribeForecast(mbRequest);
                        break;
                    case Constants.MSG_STOP_PLUGINS:
                        stopPlugins();
                        break;
                    case Constants.MSG_STOP_SENSORS:
                        stopSensors();
                        break;
                    case Constants.MSG_CONFIG_ACCOUNT_NAME:
                        configAccountName(mbRequest);
                        break;
                    case Constants.MSG_GOOGLE_PLAY_AVAILABLE_ERROR:
                        showGooglePlayServicesError(mbRequest);
                        break;
                    case Constants.MSG_SENSOR_SETTINGS:
                        changeSensorSettings(mbRequest);
                        break;
                    case Constants.MSG_GOOGLE_PLAY_AVAILABLE:
                        isGooglePlayServicesAvailable();
                        break;
                    case Constants.MSG_GET_CALENDAR_EVENTS:
                        getCalendarEvents();
                        break;
                    case Constants.MSG_RESET_CURRENT_LOCATION:
                        resetCurrentLocation();
                        break;
                    case Constants.MSG_SET_LOCATION_SETTINGS:
                        setLocationSettings(mbRequest);
                        break;
                    case Constants.MSG_RESET_HISTORY_LOCATIONS:
                        resetHistoryLocations();
                        break;
                    case Constants.MSG_SET_STREAMING_SETTINGS:
                        setStreamingSettings(mbRequest);
                        break;
                    case Constants.MSG_STREAMING_SUBSCRIBE:
                        subscribeStreaming(mbRequest);
                        break;
                    case Constants.MSG_STREAMING_UNSUBSCRIBE:
                        unsubscribeStreaming(mbRequest);
                        break;
                    case Constants.MSG_STREAMING_STOP_STREAM:
                        stopStreaming(mbRequest);
                        break;
                    default:
                        Toast.makeText(mContext, "The MBRequest object has a non valid id",
                                Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                post(request);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * This is an asynchronous request like {@link #send(Object)} but the difference is that
     * this method requires that the response event be delivered to only the caller subscriber,
     * not to all the objects that are subscribed to this event.
     * @param subscriber This is the subscriber object that will receive the response
     * @param request This is the request message
     * @param event this is the event that the subscriber will handle on its onEvent(event) method
     */
    public void sendAndReceive(Object subscriber, Class event, Object request){
        SubscriberEvent subsEvent = new SubscriberEvent( event, subscriber );
        mSubscribers.put(request.hashCode(), subsEvent);
        send( request );
    }


    /**
     * This method checks whether the event has a valid request id. If so, it looks up the
     * corresponding subscriber for this event and set it into the event object
     * @param event
     */
    private void setSubscriber( Object event ){
        if( event instanceof BaseEvent ){
            SubscriberEvent subsEvent = mSubscribers.remove( event.hashCode() );
            if (subsEvent != null && subsEvent.getEvent() == event.getClass()) {
                Object subscriber = subsEvent.getSubscriber();
                ((BaseEvent) event).setSubscriber(subscriber);
            }
        }
    }


    /**
     * Use this method to send a synchronous request and get an immediate result. The request should
     * not take too much processing time, so it warranties not to block the main thread (UI thread).
     * @param request
     * @return
     */
    public Object get( MBRequest request ){
        Object result = null;
        switch ( request.getRequestId() ){
            case Constants.MSG_GET_USER_PROFILE:
                result = getUserProfile( request );
                break;
            case Constants.MSG_GET_NEWS_ITEMS:
                result = getNewsItems( request );
                break;
            case Constants.MSG_APPLY_FILTERS:
                result = applyFilters(request);
                break;
            case Constants.MSG_GET_ARTICLE_POSITION:
                result = getArticle( request );
                break;
            case Constants.MSG_GET_MODEL_DISTRIBUTIONS:
                result = getModelDistributions( request );
                break;
            case Constants.MSG_DEVICE_ONLINE:
                result = isDeviceOnline( );
                break;
            case Constants.MSG_GET_SELECTED_ACCOUNT_NAME:
                result = getSelectedAccountName();
                break;
            case Constants.MSG_GET_AWARE_SETTINGS:
                result = getAwareSetting( request );
                break;
            case Constants.MSG_WEATHER_CHANGE_FORECAST_MODE:
                result = changeForecastMode( request );
                break;
            case Constants.MSG_SEARCH_HOTEL:
                result = searchHotel(request);
                break;
            case Constants.MSG_STATUS_BATTERY:
                result = getStatusBattery();
                break;
            case Constants.MSG_GET_AR_NAME:
                result = getARName(request);
                break;
            case Constants.MSG_WEATHER_GET_REPORT:
                result = getWeatherReport(request);
                break;
            case Constants.MSG_GET_LOCATION:
                result = getLocation( request );
                break;
            case Constants.MSG_GET_HISTORY_LOCATIONS:
                result = getHistoryLocations( );
                break;
            default:
                Toast.makeText( mContext, "The MBRequest object has a non valid id",
                        Toast.LENGTH_LONG).show();
                break;
        }
        return result;
    }


    public static void set(MBRequest mbRequest) {
        switch ( mbRequest.getRequestId() ){
            case Constants.SET_NEWS_LIST_SIZE:
                setNewsSize( (int) mbRequest.getValues()[0] );
                break;
            case Constants.SET_REFRESH_TIME:
                setNewsRefreshTime((long) mbRequest.getValues()[0]);
                break;
            case Constants.SET_UPDATE_TIME:
                setNewsUpdateTime( (long) mbRequest.getValues()[0] );
                break;
            default:
                break;
        }
    }

    /**
     * Use this method to subscribe to messages that are published by other components. Using this
     * method will allow you implement overloaded versions of onEvent method (a event handler method)
     * @param subscriber
     */
    public void subscribe( Object subscriber ) {
        if( !mEventBus.isRegistered( subscriber ) ) {
            mEventBus.register(subscriber);
            mServiceLocator.addActivity( subscriber );
        }
    }


    /**
     * This method subscribes the subscriber object to a specific event updates. However, unless
     * {@link #subscribe(Object)}, this method set the update process to a hold-on state until
     * explicit notification from the subscriber of being ready to start receiving updates is sent.
     * @param subscriber
     * @param event
     */
    public void subscribe( Object subscriber, Class... event ) {
        //add a subscription exception to avoid receiving updates until the subscriber is ready
        for( Class e : event ) {
            mEventBus.addSubscriptionException(subscriber, e);
        }
        if( !mEventBus.isRegistered( subscriber ) ) {
            mEventBus.register(subscriber);
        }
    }


    /**
     * Use this method to unsubscribe to any kind of messages published by other components. This is
     * useful to improve the resource management, for instance, use this method when the activity
     * is paused (onPause method) and subscribe again when it resumes (onResume method)
     * @param subscriber
     */
    public void unsubscribe(Object subscriber) {
        mEventBus.unregister(subscriber);
    }


    public void unsubscribe(Object subscriber, Class event){
        mEventBus.addSubscriptionException(subscriber, event);
    }

    public void removeSubscriptionException( Object subscriber, Class event ){
        mEventBus.removeSubscriptionException(subscriber, event);
    }


    /**
     * This method posts a sticky event, that is, the event is cached by the message broker so you
     * can get it anytime by using getSticky method. This is for internal use of the middleware.
     * @param event
     */
    public void postSticky( Object event ){
        setSubscriber( event );
        mEventBus.postSticky(event);
    }

    private void post( Object event ){
        setSubscriber( event );
        mEventBus.post(event);
    }

    /**
     * This method gets a specific event which has been cached by the message broker. This is for
     * internal use of the middleware.
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> T getStickyEvent( Class<T> eventType ){
        return mEventBus.getStickyEvent( eventType );
    }


    // ****************************** CONTROLLER CALLS *******************************************

    // ***** STREAMING ****************************************************************************

    private void setStreamingSettings(MBRequest mbRequest) {
        StreamingService streamingService = mServiceLocator.getService( StreamingService.class );
        String videoQuality = (String) mbRequest.get( Constants.SET_VIDEO_QUALITY );
        if( videoQuality != null && !videoQuality.isEmpty() ){
            streamingService.setVideoQuality( videoQuality );
        }
        Boolean toggleStream = (Boolean) mbRequest.get( Constants.SET_TOGGLE_STREAM );
        if( toggleStream != null && toggleStream ){
            streamingService.toogleStream( (String) mbRequest.get( Constants.STREAMING_USERNAME ),
                    (String) mbRequest.get( Constants.STREAMING_PASSWORD ),
                    (String) mbRequest.get( Constants.STREAMING_DESTINATION ));
        }
        Boolean toggleFlash = (Boolean) mbRequest.get( Constants.SET_STREAMING_TOGGLE_FLASH );
        if( toggleFlash != null && toggleFlash ){
            streamingService.toggleFlash();
        }
        Boolean switchCamera = (Boolean) mbRequest.get( Constants.SET_STREAMING_SWITCH_CAMERA );
        if( switchCamera != null && switchCamera ){
            streamingService.switchCamera( );
        }
    }

    private void stopStreaming(MBRequest mbRequest) {
        mServiceLocator.getService(StreamingService.class).stopStreaming(
                (String) mbRequest.get(Constants.STREAMING_DESTINATION));
    }

    private void unsubscribeStreaming(MBRequest mbRequest) {
        mServiceLocator.getService(StreamingService.class).unsubscribe(
                (StreamingSubscriptionVO) mbRequest.get(Constants.STREAMING_SUBCRIPTION_CONFIG));
    }

    private void subscribeStreaming(MBRequest mbRequest) {
        mServiceLocator.getService(StreamingService.class).subscribe(
                (StreamingSubscriptionVO) mbRequest.get(Constants.STREAMING_SUBCRIPTION_CONFIG));
    }


    // ***** NEWS ********************************************************************************

    private void showPreviousArticle( MBRequest request ) throws Exception{
        request.put( Constants.BUNDLE_ARTICLE_ID, Constants.ARTICLE_PREVIOUS_POSITION );
        validateNewsService( NewsService.class.getMethod("showArticle", MBRequest.class), request );
    }

    private void showCurrentArticle( MBRequest request ) throws Exception{
        validateNewsService( NewsService.class.getMethod("showArticle", MBRequest.class), request );
    }

    private void showNextArticle( MBRequest request ) throws Exception{
        request.put( Constants.BUNDLE_ARTICLE_ID, Constants.ARTICLE_NEXT_POSITION );
        validateNewsService( NewsService.class.getMethod("showArticle", MBRequest.class), request );
    }

    private void launchNewsActivity( MBRequest request ) throws Exception{
        validateNewsService( NewsService.class.getMethod("startNewsActivity", MBRequest.class), request);
    }

    private void requestNewsItems( MBRequest request ) throws Exception{
        validateNewsService( NewsService.class.getMethod("loadNewsItems", MBRequest.class), request);
    }

    private void validateNewsService( Method method, MBRequest request, Object... args){
        NewsService newsService = mServiceLocator.getService(NewsService.class);
        if (newsService == null) {
            request.put(Constants.BUNDLE_MESSAGE_TYPE, "NEWS");
            mRequests.add(request);
        } else {
            try {
                if( args == null || args.length == 0){
                    method.invoke(newsService, request);
                }else {
                    method.invoke(newsService, request, args);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private NewsArticleVector getNewsItems( MBRequest request ) {
        return mServiceLocator.getService(NewsService.class).getNewsItems(request);
    }

    private void filterNewsByEmail( MBRequest request ) throws Exception{
        validateNewsService(NewsService.class.getMethod("filterNewsByEmail", MBRequest.class), request);
    }

    /**
     * This method can be used to change programmatically the time interval for the list of news
     * items to be refreshed from the Yahoo' news server. This time interval cn also be defined in
     * your midd_config.properties file
     * @param millis
     */
    public static void setNewsRefreshTime( long millis ){
        NewsService.setRefreshTime(millis);
    }

    /**
     * This method can be used to change programmatically the time interval for the list of news
     * items to be automatically updated from the Yahoo' news server. The difference between this
     * method and setNewsRefreshTime method is that the latter is a time interval used when you
     * explicitly request the list of news articles whereas the former is used by a timer task that
     * automatically retrieves the latest list of news articles. This time interval cn also be defined
     * in your midd_config.properties file
     * @param millis
     */
    private static void setNewsUpdateTime( long millis ){
        NewsService.setmUpdateTime(millis);
    }

    private static void setNewsSize(int newsSize) {
        NewsService.setNewsListSize(newsSize);
    }

    private void login( MBRequest request ) throws Exception{
        validateNewsService( NewsService.class.getMethod("login", MBRequest.class), request );
        mServiceLocator.getService(NewsService.class).login(request);
    }

    private UserProfile getUserProfile( MBRequest request ){
        return mServiceLocator.getService(NewsService.class).requestUserProfile(request);
    }

    private NewsArticleVector applyFilters(MBRequest request) {
        return mServiceLocator.getService(NewsService.class).applyFilter(request,
                (NewsArticleVector) request.get(Constants.CONTENT_NEWS_LIST));
    }

    private void showArticle(MBRequest mbRequest) throws Exception{
        validateNewsService(NewsService.class.getMethod("showArticle", MBRequest.class), mbRequest);
    }

    private void expandArticle(MBRequest mbRequest) throws Exception{
        validateNewsService( NewsService.class.getMethod("expandArticle", MBRequest.class), mbRequest );
    }

    private Integer getArticle(MBRequest mbRequest) {
        return mServiceLocator.getService(NewsService.class).getArticle(mbRequest);
    }

    private ModelDistribution getModelDistributions( MBRequest mbRequest ){
        return mServiceLocator.getService(NewsService.class).getModelDistributions(mbRequest);
    }

    /**
     * This event handler processes both the explicit request of the news list and the automatic
     * update of this list
     * @param event
     */
    public void onEventMainThread( ResponseFetchNewsEvent event ){
        NewsService newsService = mServiceLocator.getService(NewsService.class);
        Boolean updateNews = newsService.getRequests().get(event.getMbRequestId()) == null? null
                : (Boolean) newsService.getRequests().get(event.getMbRequestId()).
                get(Constants.FLAG_UPDATE_NEWS);
        if( updateNews != null && updateNews == true ){
            newsService.getNewsUpdate(newsService.getRequests().remove(event.getMbRequestId()));
        }else{
            newsService.getNewsItemList(event.getMbRequestId(), newsService.getRequests().
                    get(event.getMbRequestId()));
        }
    }


    // ***** STREAMING *******************************************************************************

    /**
     * @param mbRequest this MBRequest object should contain:
     *                  Constants.HTTP_REQUEST_SERVER_URL: the destination server URL
     *                  Constants.HTTP_REQUEST_BODY: body of the request. It could be:
     *                      - a File Object
     *                      - a byte[] array
     *                      - an InputStream
     *                      - a String (location of the resource to be uploaded in the phone's sdcard memory)
     *                      - a BitMap
     *                  Constants.HTTP_REQUEST_PARAMS: if no body is provided, then it uses the params
     *                  Constants.HTTP_REQUEST_CONNECTION_TIMEOUT: Connection time in miliseconds
     *                  Constants.IMG_COMPRESS_FORMAT: The format of the compressed image (e.g.,JPEG, PNG, etc.)
     *                  Constants.IMG_QUALITY: Hint to the compressor, 0-100. 0 meaning compress for small size,
     *                  100 meaning compress for max quality. Some formats, like PNG which is lossless,
     *                  will ignore the quality setting
     */
    private void uploadToServer(MBRequest mbRequest) {
        FileUploader.upload(mbRequest);
    }

    private void recordAudio(MBRequest mbRequest) {
        mEventBus.removeSubscriptionException(mbRequest.get(Constants.SET_SUBSCRIBER), AudioRecordEvent.class);
        AudioController.getInstance((Integer) mbRequest.get(
                        Constants.SET_AUDIO_SAMPLE_RATE),
                (Integer) mbRequest.get(Constants.SET_AUDIO_CHANNEL_CONFIG),
                (Integer) mbRequest.get(Constants.SET_AUDIO_ENCODING),
                (Integer) mbRequest.get(Constants.SET_AUDIO_BUFFER_ELEMENTS_TO_REC),
                (Integer) mbRequest.get(Constants.SET_AUDIO_BYTES_PER_ELEMENT),
                mMB,
                mbRequest.get(Constants.SET_SUBSCRIBER),
                (String) mbRequest.get(Constants.HTTP_FILE_EXTENSION),
                (String) mbRequest.get(Constants.HTTP_MIME_TYPE));
    }

    private void stopRecordAudio(MBRequest request){
        unsubscribe(request.get(Constants.SET_SUBSCRIBER), AudioRecordEvent.class);
        AudioController.getInstance(this).unsubscribe(request.get(Constants.SET_SUBSCRIBER));
    }



    // ***** CALENDAR ******************************************************************************


    private void processEventsCalendar(MBRequest mbRequest){
        mServiceLocator.getService(CalendarService.class)
                .processEvents((Integer) mbRequest.get(Constants.CALENDAR_MODE),
                        mbRequest.get(Constants.CALENDAR_EVENT_DATA));
    }

    private void getCalendarEvents() {
        mServiceLocator.getService(CalendarService.class).getCalendarEvents();
    }

    // ***** LOCATION ******************************************************************************

    private void resetHistoryLocations() {
        mServiceLocator.getService(LocationService.class).resetHistoryLocations();
    }

    private void setMaxNumHistoryLocations( int maxNumber) {
        mServiceLocator.getService(LocationService.class)
                .setMaxNumHistoryLocations(maxNumber);
    }

    private void setLocationSettings(MBRequest request) {
        LocationService locationService = mServiceLocator.getService(LocationService.class);
        Boolean writeReadFromFile = (Boolean) request.get( Constants.LOCATION_WRITE_READ_DATA_FROM_FILE);
        if( writeReadFromFile != null ){
            locationService.writeLocationDataToFile(writeReadFromFile);
        }
        Boolean enableHistory = (Boolean) request.get( Constants.LOCATION_HISTORY);
        if( enableHistory != null ){
            locationService.setEnableHistoryLocation(enableHistory);
        }
        Integer maxNumberHistoryLocations = (Integer) request.get( Constants.LOCATION_MAX_HISTORY );
        if( maxNumberHistoryLocations != null ){
            locationService.setMaxNumHistoryLocations(maxNumberHistoryLocations);
        }
    }

    private void setLocation(MBRequest request){
        String place = (String) request.get( Constants.LOCATION_CURRENT_PLACE );
        LocationService locationService = mServiceLocator.getService(LocationService.class);
        if( place != null && !place.isEmpty() ){
            locationService.setDefaultCurrentPlace(place);
        }else {
            locationService.getCurrentLocationByCoordinates((Double) request.get(Constants
                    .LOCATION_LATITUDE), (Double) request.get(Constants.LOCATION_LONGITUDE));
        }
    }

    private void resetCurrentLocation() {
        mServiceLocator.getService(LocationService.class).resetCurrentLocation();
    }


    private LocationEvent getLocation( MBRequest request ){
        String place = (String) request.get( Constants.LOCATION_PLACE_NAME );
        LocationService locationService = mServiceLocator.getService(LocationService.class);
        if( place != null && !place.isEmpty() ){
            if( place == Constants.LOCATION_CURRENT_PLACE ){
                return locationService.fillEvent( locationService.obtainCurrentLocation() );
            }else{
                return locationService.fillEvent( locationService.getPlaceLocation(
                        new LocationVO( place ), true) );
            }
        }else{
            Double latitude = Double.valueOf( (String) request.get( Constants.LOCATION_LATITUDE));
            Double longitude = Double.valueOf( (String) request.get( Constants.LOCATION_LONGITUDE));
            if( latitude != null && longitude != null ){
                return locationService.fillEvent( locationService.getPlaceLocation(
                        new LocationVO( latitude, longitude ), true) );
            }
        }
        return null;
    }


    private ArrayList<LocationEvent> getHistoryLocations() {
        return mServiceLocator.getService( LocationService.class ).getHistoryLocations();
    }


    // ***** HOTEL ******************************************************************************

    private ArrayList<HotelVO> searchHotel(MBRequest request){
        return mServiceLocator.getService(HotelReservationService.class)
                .search((HotelSearchCriteria) request.get(Constants.HOTEL_CRITERIA));
    }


    // ***** WEATHER ******************************************************************************

    private Integer changeForecastMode( MBRequest request ) {
        return mServiceLocator.getService(WeatherService.class)
                .changeForecastMode((String) request.get(Constants.WEATHER_PLACE));
    }

    public void subscribeForecast(MBRequest request){
        Boolean isLocationActive = Boolean.valueOf(AwareServiceWrapper.getSetting(mContext,
                LocationSettings.STATUS_GOOGLE_FUSED_LOCATION));
        if( !isLocationActive ){
            AwareServiceWrapper.startPlugin(mContext, Constants.SERVICE_LOCATION);
        }
        LocationVO locationVO = new LocationVO();
        String place = (String) request.get(Constants.WEATHER_PLACE);
        if( place != null && !place.trim().equals("") ){
            locationVO.setSubArea((String) request.get(Constants.WEATHER_PLACE));
        }else {
            locationVO.setCountry((String) request.get(Constants.LOCATION_COUNTRY));
            locationVO.setCity((String) request.get(Constants.LOCATION_CITY));
            locationVO.setCountryCode((String) request.get(Constants.LOCATION_COUNTRY_CODE));
            locationVO.setStateCode((String) request.get(Constants.LOCATION_STATE_CODE));
            locationVO.setSubArea((String) request.get(Constants.WEATHER_PLACE));
        }
        int forecastMode = request.get(Constants.WEATHER_MODE) != null? (Integer)
                request.get(Constants.WEATHER_MODE) : -1;
        long refreshTime = request.get(Constants.WEATHER_REFRESH_TIME) != null? (Long)
                request.get(Constants.WEATHER_REFRESH_TIME) : -1;
        boolean forceRefresh = request.get(Constants.WEATHER_FORCE_REFRESH) != null? (Boolean)
                request.get(Constants.WEATHER_FORCE_REFRESH) : false;
        mServiceLocator.getService(WeatherService.class).findWeatherData(locationVO,
                forecastMode, refreshTime, forceRefresh,
                (List)request.get(Constants.WEATHER_CONDITION_RULES), true);
    }

    public void unsubscribeForecast(MBRequest request){
        String place = (String) request.get(Constants.WEATHER_PLACE);
        if( place != null && !place.equals("") ) {
            mServiceLocator.getService(WeatherService.class).unsubscribe(place);
        }
    }


    private List getWeatherReport(MBRequest request){
        Boolean isLocationActive = Boolean.valueOf(AwareServiceWrapper.getSetting(mContext,
                LocationSettings.STATUS_GOOGLE_FUSED_LOCATION));
        if( !isLocationActive ){
            AwareServiceWrapper.startPlugin(mContext, Constants.SERVICE_LOCATION);
        }
        int forecastMode = request.get(Constants.WEATHER_MODE) != null? (Integer)
                request.get(Constants.WEATHER_MODE) : -1;
        String place = (String) request.get(Constants.WEATHER_PLACE);
        if( forecastMode == Constants.WEATHER_FORECAST_DAILY ) {
            return mServiceLocator.getService(WeatherService.class).getDailyReport(place);
        }else{
            return mServiceLocator.getService(WeatherService.class).getHourlyReport(place);
        }
    }

    // ****************************** ACTIVITY RECOGNITION *****************************************

    private String getARName(MBRequest request) {
        return ActivityRecognitionService.getActivityName((Integer)
                request.get(Constants.AR_ACTIVITY_TYPE));
    }


    // ***** COMMON ******************************************************************************

    private void launchActivity( MBRequest request ){
        try {
            Class clazz = null;
            if (request.get(Constants.BUNDLE_ACTIVITY_NAME) == null) {
                throw new Exception("The name of the activity doesn't exist. MB cannot create a new activity");
            } else {
                try {
                    clazz = Class.forName((String) request.get(Constants.BUNDLE_ACTIVITY_NAME));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(mContext, clazz);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mServiceLocator.addActivityClass( clazz );
            mContext.startActivity( intent );
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Context getContext(){
        return mContext;
    }

    public static void setContext(Context context) {
        MessageBroker.mContext = context;
    }


    private void showGooglePlayServicesError(MBRequest mbRequest) {
        try {
            Integer connection = (Integer) mbRequest.get(Constants.GOOGLE_CONNECTION_STATUS);
            Activity activity = (Activity) mbRequest.get(Constants.APP_CONTEXT);
            UtilServiceAPIs.showGooglePlayServicesAvailabilityErrorDialog(connection, activity);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void processRequests(){
        synchronized (mRequests) {
            for (MBRequest request : mRequests) {
                if (request.get(Constants.BUNDLE_MESSAGE_TYPE) != null
                        && (request.get(Constants.BUNDLE_MESSAGE_TYPE)).equals("NEWS")) {
                    MessageBroker.getInstance(mContext).send( request );
                    mRequests.remove(request);
                }
            }
        }
    }

    private void chooseAccount(MBRequest request){
        try {
            UtilServiceAPIs.chooseAccount(((Activity) request.get(Constants.BUNDLE_ACTIVITY_NAME)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isDeviceOnline( ){
        return UtilServiceAPIs.isDeviceOnline(mContext);
    }

    private String getSelectedAccountName() {
        return UtilServiceAPIs.credential.getSelectedAccountName();
    }

    private boolean isGooglePlayServicesAvailable() {
        return UtilServiceAPIs.isGooglePlayServicesAvailable(mContext);
    }

    private void configAccountName(MBRequest mbRequest) {
        if( UtilServiceAPIs.configAccountName(
                (Activity) mbRequest.get(Constants.BUNDLE_ACTIVITY_NAME),
                (String) mbRequest.get(Constants.ACCOUNT_NAME)) ){
            mServiceLocator.getService(CalendarService.class).initializeCalendar();
        }
    }

    // ****************************** AWARE *************************************************

    private boolean getStatusBattery(){
        return AwareServiceWrapper.getSetting(mContext,
                Aware_Preferences.STATUS_BATTERY).equals("true")? true : false;
    }

    private void stopSensors() {
        AwareServiceWrapper.stopSensors();
    }

    private void stopPlugins() {
        AwareServiceWrapper.stopPlugins();
    }

    private String getAwareSetting(MBRequest request) {
        return AwareServiceWrapper.getSetting(mContext,
                (String) request.get(Constants.SERVICE_SETTINGS));
    }

    private void startAwarePlugin(MBRequest request){
        AwareServiceWrapper.startPlugin(mContext, request);
    }

    private void stopAwarePlugin(MBRequest request){
        AwareServiceWrapper.stopPlugin(mContext, request);
    }

    private void changeSensorSettings(MBRequest mbRequest) {
        String sensorName = (String) mbRequest.get( Constants.SENSOR_NAME );
        if( sensorName != null ){
            Object sensorSetting = mbRequest.get( Constants.SENSOR_SETTING );
            if( sensorSetting.equals( Constants.SENSOR_START ) ){
                mServiceLocator.startSensor(sensorName);
            }else if( sensorSetting.equals( Constants.SENSOR_STOP ) ){
                mServiceLocator.stopSensor(sensorName);
            }

            if( sensorName.equals( Constants.SENSOR_ACCELEROMETER )){
                Long frequency = (Long) mbRequest.get( Constants.ACCELEROMETER_FREQUENCY );
                if( frequency != null ){
                    mServiceLocator.getSensor(AccelerometerObserver.class).setFrequency( frequency );
                }
            }
        }
    }

    // ****************************** CACHE MEMORY *************************************************

    /**
     * This method adds an object to the mCache memory
     * @param object object to be added
     * @param id this is used to retrieve the object
     */
    public void addObjToCache(Object id, Object object) {
        synchronized (mCache) {
            mCache.put(id, object);
        }
    }

    /**
     * This method retrieves an object from the mCache memory
     * @param id of the object
     * @param remove whether the object must be removed from the mCache or not
     * @return
     */
    public Object getObjFromCache( Object id , boolean remove ){
        synchronized (mCache) {
            if (remove) {
                return mCache.remove(id);
            }
            return mCache.get(id);
        }
    }

}
