package com.yahoo.inmind.your_app.view;

import android.app.Activity;
import android.content.Intent;

import com.yahoo.inmind.middleware.control.MessageBroker;
import com.yahoo.inmind.middleware.events.MBRequest;
import com.yahoo.inmind.middleware.events.news.NewsResponseEvent;
import com.yahoo.inmind.model.FilterVO;
import com.yahoo.inmind.model.NewsArticle;
import com.yahoo.inmind.model.NewsArticleVector;
import com.yahoo.inmind.model.slingstone.UserProfile;
import com.yahoo.inmind.reader.ReaderMainActivity;
import com.yahoo.inmind.util.Constants;
import com.yahoo.inmind.util.Util;
import com.yahoo.inmind.your_app.R;
import com.yahoo.inmind.your_app.control.SingletonApp;

import java.util.ArrayList;

/**
 * Created by oscarr on 1/3/15.
 */
public class ViewHelper {
    private static ViewHelper instance;
    private MessageBroker mMB;
    private Activity mActivity;

    /**
     * Some constants that you may define in order to decide what to do with the list of news articles ...
     */
    private static final int DECISION_ONE = 0;
    private static final int DECISION_TWO = 1;
    private static final int DECISION_THREE = 2;
    private static final String CONTENT_ID = "MY_CACHE_ID";
    private int position = 0;

    private ViewHelper(Activity act) {
        // Controllers
        SingletonApp.getInstance( act.getApplicationContext() );
        mActivity = act;
        mMB = SingletonApp.mMB;
        mMB.subscribe( this );

        // By default, the news reader retrieves a list of 170 news articles. However,
        // you can decrease the size of this list by doing this:
        MessageBroker.set( new MBRequest( Constants.SET_NEWS_LIST_SIZE, 40 ) );

        // By default, the news reader caches for 2 minutes the latest version of the
        // news articles list before triggering a new request to the Yahoo server.
        // This is defined in the midd_config.properties file. However, you can change
        // this value programmatically (in miliseconds):
        MessageBroker.set( new MBRequest( Constants.SET_REFRESH_TIME, 180000 ) );

        // By default, the news reader automatically triggers an event every hour to
        // check if there is any update in the Yahoo News server. If not, no notification
        // is sent.
        MessageBroker.set( new MBRequest( Constants.SET_UPDATE_TIME, 7200000 ) );
    }

    public static ViewHelper getInstance(Activity a) {
        if (instance == null) {
            instance = new ViewHelper(a);
        }
        return instance;
    }


    // ****************************** CALLS TO THE NEWS READER *************************************

    /**
     * It opens the provided (default) news reader activity and shows the news articles
     */
    public void showDefaultReader(){
        mMB.send(new MBRequest(Constants.MSG_LAUNCH_BASE_NEWS_ACTIVITY));
    }

    /**
     * It opens the extended (personalized) news reader activity and shows the news articles
     */
    public void showExtendedReader(){
        MBRequest request = new MBRequest( Constants.MSG_LAUNCH_EXT_NEWS_ACTIVITY );
        request.put(Constants.BUNDLE_ACTIVITY_NAME, NewsExtendedActivity.class.getCanonicalName());
        mMB.send(request);
    }

    /**
     * It triggers a service which will query an updated list of news articles.
     * Since the process is asynchronous, the results of this call are handled by the
     * onEvent(NewsResponseEvent event) method below. You can add qualifiers (optional) to your
     * request so you can decide what to do with the results when they are caught by your event
     * handler method (here, DECISION_ONE and DECISION_TWO are just samples of control flow. Check
     * the event handler below).
     */
    public void listNewsItems(){
        MBRequest request = new MBRequest( Constants.MSG_REQUEST_NEWS_ITEMS );
        request.put(Constants.QUALIFIER_NEWS, DECISION_ONE);
        // You may force the News Reader to reload the more recent list of news articles
        // by uncommenting this line (flag). Otherwise, the system will decide whether to
        // return a cached list or an updated list (depending on performance issues)
        // request.put( Constants.FLAG_FORCE_RELOAD, true );
        mMB.sendAndReceive( this, request, NewsResponseEvent.class );
    }

    /**
     * This is an alternative method (similar to listNewsItems method) but this time we add a different
     * qualifier, so the Event Handler will process the results in a different way. This method also
     * includes a flag indicating that you need the response to be in json format.
     */
    public void listNewsItems2(){
        MBRequest request = new MBRequest( Constants.MSG_REQUEST_NEWS_ITEMS);
        request.put(Constants.QUALIFIER_NEWS, DECISION_TWO);
        // this will return the response in json format
        request.put(Constants.FLAG_RETURN_JSON, true);
        mMB.sendAndReceive( this, request, NewsResponseEvent.class );
    }

    /**
     * This is an example of how you can filter the list of news articles. Results are handled by
     * onEvent(NewsResponseEvent event) method below.
     */
    public void showFilteredNews(){
        // add as many filters as you need
        ArrayList<FilterVO> filters = new ArrayList<>();
        filters.add( new FilterVO( Constants.ARTICLE_SCORE, Constants.FILTER_HIGHER_THAN, "0.7") );
        filters.add( new FilterVO( Constants.ARTICLE_SUMMARY, Constants.FILTER_CONTAINS_STRING, "and") );

        // now, you can either:
        // 1. Request a filtered list of news, receive the results on onEvent(NewsResponseEvent)
        // method (case DECISION_TWO), and show the list in a new activity.
        MBRequest request = new MBRequest( Constants.MSG_REQUEST_NEWS_ITEMS);
        request.put(Constants.BUNDLE_FILTERS, filters);
        request.put(Constants.QUALIFIER_NEWS, DECISION_TWO);
        mMB.sendAndReceive( this, request, NewsResponseEvent.class );

        // 2. Or get the results immediately so you can do more operations over the list.
        // This alternative assumes that you have already requested the most recent list of news articles
        // in a previous step (for that, you should first call either any implementation of the news
        // reader activity or the listNewsItems method above.) Uncomment the lines below:
//        MBRequest request1 = new MBRequest( Constants.MSG_APPLY_FILTERS);
//        NewsArticleVector vec = (NewsArticleVector) mMB.get( new MBRequest(Constants.MSG_GET_NEWS_ITEMS) );
//        request1.put(Constants.CONTENT_NEWS_LIST, vec);
//        request1.put(Constants.BUNDLE_FILTERS, filters);
//        NewsArticleVector list = (NewsArticleVector) mMB.get( request1 );
        // ....
    }


    /**
     * This method takes the news item list returned by a previous call (e.g., triggerService),
     * modifies each item by adding a prefix (counter) in the title and then show the modified
     * news in a new activity
     */
    public void showModifiedNews() {
        int prefix = 1;
        NewsArticleVector vector = (NewsArticleVector) mMB.get( new MBRequest(Constants.MSG_GET_NEWS_ITEMS) );
        for ( NewsArticle item : vector ){
            item.setTitle( prefix++ + "  **** " + item.getTitle() );
        }
        MBRequest request = new MBRequest( Constants.MSG_SHOW_MODIFIED_NEWS);
        request.put(Constants.BUNDLE_MODIFIED_NEWS, vector);
        request.put(Constants.BUNDLE_ACTIVITY_NAME, ReaderMainActivity.class.getCanonicalName());
        mMB.send(request);
    }

    /**
     * This method opens a News Reader activity with a customized layout and UI components
     */
    public void showCustomizedReader(){
        MBRequest request = new MBRequest( Constants.MSG_LAUNCH_EXT_NEWS_ACTIVITY );
        request.put( Constants.BUNDLE_ACTIVITY_NAME, CustomizedLayoutActivity.class.getCanonicalName());

        // Add as many UI components as you need (taken from your layout) to override the ones of
        // the news reader. Specify the id like R.id....
        request.put( Constants.UI_LANDSCAPE_LAYOUT, R.layout.app_reader_landscape_layout );
        request.put( Constants.UI_PORTRAIT_LAYOUT, R.layout.app_reader_portrait_layout );

        // widgets
        request.put( Constants.UI_NEWS_FEAT, R.id.app_news_feat );
        request.put( Constants.UI_NEWS_FEAT2, R.id.app_news_feat2 );
        request.put( Constants.UI_NEWS_RANK, R.id.app_news_rank );
        request.put( Constants.UI_NEWS_TITLE, R.id.app_news_title );
        request.put( Constants.UI_NEWS_SUMMARY, R.id.app_news_summary );
        request.put( Constants.UI_NEWS_REASON, R.id.app_news_reason );
        request.put( Constants.UI_NEWS_SCORE, R.id.app_news_score );
        request.put( Constants.UI_NEWS_IMG, R.id.app_news_img );
        request.put( Constants.UI_NEWS_PUBLISHER, R.id.app_news_publisher );
        request.put( Constants.UI_NEWS_SHARE_FB, R.id.app_news_btnShareFb );
        request.put( Constants.UI_NEWS_SHARE_TWITTER, R.id.app_news_btnShareTwitter );
        request.put( Constants.UI_NEWS_SHARE_TMBLR, R.id.app_news_btnShareTumblr );
        request.put( Constants.UI_NEWS_SHARE_MORE, R.id.app_news_btnShareMore );
        mMB.send(request);
    }


    /**
     * This method opens the login activity. The results of this action are handled by the
     * onActivityResult() method of the the bound activity (which is passed as a reference through
     * the request object)
     */
    public void login() {
        MBRequest request = new MBRequest( Constants.MSG_LOGIN );
        // If you wish to handle the results of this call then specify RESULTS_LOGIN as an int >= 0
        // and add the parent activity which will handle the result to the request object (CONTENT).
        // The results of this action will be handled by the onActivityResult() method of the bound
        // activity (mActivity). If you don't wish to handle the result then specify RESULTS_LOGIN
        // as -1 and then send the request without providing the activity's reference (CONTENT).
        request.put( Constants.RESULTS_LOGIN, 0 );
        request.put( Constants.CONTENT, mActivity );
        mMB.send(request);
    }


    /**
     * It shows the user's info who has signed in into the app
     */
    public void showUserProfile() {
        MBRequest request = new MBRequest( Constants.MSG_GET_USER_PROFILE);
        // this request is a synchronous call, so we don't need an event handler that process the
        // result, instead we can get the result immediately and cache it
        UserProfile userProfile = (UserProfile) mMB.get( request );
        mMB.addObjToCache( CONTENT_ID, userProfile );
        // send the results to the corresponding activity.
        Intent intent = new Intent( mActivity, UserProfileList.class );
        intent.putExtra(Constants.CONTENT, CONTENT_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.getApplicationContext().startActivity(intent);
    }



    public void newsCommands(){
        MBRequest request = new MBRequest( Constants.MSG_LAUNCH_EXT_NEWS_ACTIVITY );
        request.put(Constants.BUNDLE_ACTIVITY_NAME, CommandsActivity.class.getCanonicalName());
        mMB.send(request);
    }



    // ****************************** EVENT HANDLERS ***********************************************

    /**
     * This event handler processes the most recent list of news articles requested by either
     * listNewsItems or listNewsItems2 methods.
     * @param event
     */
    public void onEvent(NewsResponseEvent event){
        // First, make sure the event corresponds to a previous request made by this subscriber.
        if( mMB.checkRequestId( this, event ) ) {
            Intent intent;
            switch (event.getQualifier()) {
                case DECISION_ONE:
                    // once you have the news articles, process them. For instance:
                    // 1. Iterate and add a prefix to each news item (modifyItems method)
                    // 2. Set the results in the cache memory and specify an unique id for this object so
                    // you can retrieve it later in your activity. For instance, here we use an arbitrary id.
                    mMB.addObjToCache(CONTENT_ID, modifyItems(event));
                    // 3. Open a new activity and pass as reference the id of the cached object, that is,
                    // the arbitrary id. This will work as a weak reference that allows you to
                    // retrieve the results in the new activity (in this case, in your own implementation
                    // of ListActivity, let's say NewsArticleList).
                    intent = new Intent(mActivity, NewsArticleList.class);
                    intent.putExtra(Constants.CONTENT, CONTENT_ID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.getApplicationContext().startActivity(intent);
                    break;
                case DECISION_TWO:
                    mMB.addObjToCache(CONTENT_ID, event.getNews());
                    intent = new Intent(mActivity, NewsArticleList.class);
                    intent.putExtra(Constants.CONTENT, CONTENT_ID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.getApplicationContext().startActivity(intent);
                    break;
                case DECISION_THREE:
                    // your code goes here...
                    break;
            }
        }
    }

    /**
     * Helper method. Just for testing purposes. It demonstrates that you may get the list of news
     * items either as a list of NewsArticle objects or as a json string representation and then treat
     * it differently. This method extracts the summary of each news item, adds the prefix "News",
     * the corresponding dwell time and and a counter, and then adds the item to an ArrayList.
     * @return
     */
    private ArrayList<String> modifyItems( NewsResponseEvent event ) {
        ArrayList<String> items = new ArrayList<>();
        int counter = 1;
        if( event.getNews() == null || event.getNews().isEmpty() ){
            String json = event.getJsonRepresentation();
            if( json != null ) {
                // convert json string to object representation
                NewsArticleVector vector = NewsArticleVector.fromJson(json);
                for (NewsArticle item : vector) {
                    items.add("News: " + (counter++) + "  Dwell time: " + item.getDwellTime() + " secs. "
                            + "  Like: " + item.isLike() + " \n" + item.getSummary());
                }
            }
        }else{
            for ( NewsArticle item : event.getNews() ) {
                items.add( "News:" + (counter++) + "  Dwell time: " + item.getDwellTime() + " secs."
                        + "  Like: " + item.isLike() + " \n" + item.getSummary() );
            }
        }
        return items;
    }


    // ****************************** EXTRAS ***********************************************

    /**
     * Do not implement this functionality in your code since it will kill all the processes.
     * This is just for testing purposes
     */
    public void exit(){
        mActivity.finish();
        mMB.destroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit( 0 );
    }

}
