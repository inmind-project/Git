package com.yahoo.inmind.services.news.control.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.util.concurrent.SettableFuture;
import com.yahoo.inmind.comm.news.model.NewsResponseEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.HttpController;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.middleware.R;
import com.yahoo.inmind.services.news.control.cache.ImgLruCacher;
import com.yahoo.inmind.services.news.control.i13n.I13N;
import com.yahoo.inmind.services.news.control.share.ShareHelper;
import com.yahoo.inmind.services.news.control.util.CookieStore;
import com.yahoo.inmind.services.news.control.util.NetworkUtil;
import com.yahoo.inmind.services.news.model.events.RefreshNewsListEvent;
import com.yahoo.inmind.services.news.model.slingstone.ModelDistribution;
import com.yahoo.inmind.services.news.model.slingstone.SlingstoneSrc;
import com.yahoo.inmind.services.news.model.slingstone.UserProfile;
import com.yahoo.inmind.services.news.model.vo.FilterVO;
import com.yahoo.inmind.services.news.model.vo.NewsArticle;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;
import com.yahoo.inmind.services.news.view.handler.DataHandler;
import com.yahoo.inmind.services.news.view.handler.NewsHandler;
import com.yahoo.inmind.services.news.view.handler.QueryNewsHandler;
import com.yahoo.inmind.services.news.view.handler.UIHandler;
import com.yahoo.inmind.services.news.view.pluggableview.AsyncListView;
import com.yahoo.inmind.services.news.view.reader.DrawerManager;
import com.yahoo.inmind.services.news.view.reader.ReaderMainActivity;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.yahoo.inmind.comm.generic.control.eventbus.EventBus;

/**
 * Created by oscarr on 12/3/14.
 */
public class ReaderController {

    public final static String TAG = "inmind";
    private static Long mRefreshInterval = null;
    private static Long mLastReload = null;
    private static HashMap<Integer, Drawable> mapDrawNewsItem;
    private static int newsSize;

    private static ReaderController control;
    private static Context mApp;
    private static SettableFuture<NewsArticleVector> future;
    private static SlingstoneSrc slingstoneSrc;
    public static boolean flagClickOnArticle;



    private LayoutInflater mInflator;
    private CookieStore mCs;

    private HandlerThread trd = new HandlerThread("JSONRetrievalTrd");
    private DataHandler mDataHandler;
    private NewsHandler mNewsHandler;
    private QueryNewsHandler tempHandler;
    private boolean bIsConnected = true;
    private Config mConfig;
    private Settings mSettings;
    private ShareHelper mShareHelper;
    public boolean isInitialized = false;
    private NewsArticleVector mNewsModified = null;
    private static AsyncListView listView;
    private static Class drawerManager = DrawerManager.class;
    private static DrawerManager drawerManagerInstance;


    /** layouts: values by default **/
    public static Integer landscape_layout = R.layout.news_list_item_flat;
    public static Integer portrait_layout = R.layout.news_list_item;
    public static int news_browser_layout_id = R.layout.news_activity_browser;
    public static int news_def_list_item_layout_id = R.layout.news_def_list_item;
    public static int news_drawer_list_item_layout_id =  R.layout.news_drawer_list_item;
    public static int news_left_drawer = R.id.news_left_drawer;
    public static int news_fragment_flipview_layout_id = R.layout.news_fragment_flipview;
    public static int news_drawer_layout = R.id.news_drawer_layout;
    public static int news_swipe_container = R.id.news_swipe_container;
    public static int news_pB1 = R.id.news_pB1;
    public static int news_wv = R.id.news_wv;
    public static int text1 = R.id.text1;
    public static int news_fragment_listview = R.layout.news_fragment_listview;
    public static int news_newslist = R.id.news_newslist;
    public static int news_content_frame = R.id.news_content_frame;
    public static int news_itemtext = R.id.news_itemtext;
    public static int news_recommendation1 = R.id.news_recommendation1;
    public static int news_recommendation2 = R.id.news_recommendation2;
    public static int news_main = R.layout.news_main;
    public static int news_drawer_icon = 0;



    /** UI components **/
    public Integer news_rank = R.id.news_rank;
    public Integer news_title = R.id.news_title;
    public Integer news_score = R.id.news_score;
    public Integer news_summary = R.id.news_summary;
    public Integer news_feat = R.id.news_feat;
    public Integer news_feat2 = R.id.news_feat2;
    public Integer news_publisher = R.id.news_publisher;
    public Integer news_reason = R.id.news_reason;
    public Integer news_img = R.id.news_img;
    public Integer news_comments = R.id.news_comments;
    private Integer news_btnShareFb = R.id.news_btnShareFb;
    private Integer news_btnShareTwitter = R.id.news_btnShareTwitter;
    private Integer news_btnShareTumblr = R.id.news_btnShareTumblr;
    private Integer news_btnShareMore = R.id.news_btnShareMore;
    private Integer news_btnLike = R.id.news_btnLike;


    private Integer news_btnDislike = R.id.news_btnDislike;
    private UserProfile userProfile;
    public boolean isApplyFilter = false;
    public boolean showCategories = false;
    private ArrayList<FilterVO> filters;
    private Integer currentArticle = 0;
    private static boolean flagRefreshAsyncListView = true;
    private static ReaderMainActivity activity;
    private static String deviceId;
    public static String typeOfFilter = "AND";
    private static ModelDistribution modelDistributions;
    public static ListView mDrawerList;
    /**
     * re-ranking lists:
     *  0: no recommendation systems, just slingstone
     *  1: only Emma's recommendation system
     *  2: only William's recommendation system
     *  3: merging re-ranked lists
     */
    private static int rankingOption = 0;


    private ReaderController(){
        bIsConnected = new NetworkUtil(mApp).hasConnectivity();
        ImgLruCacher.purgeDiskCache();

        trd.start();
        mDataHandler = new DataHandler(trd.getLooper());

        mInflator = (LayoutInflater) mApp.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCs = new CookieStore(mApp);

        mConfig = new Config();
        mConfig.loadConfig();

        mSettings = new Settings();
        mShareHelper = new ShareHelper();

        mNewsHandler = new QueryNewsHandler( );
        mapDrawNewsItem = new HashMap<>();

        newsSize = Integer.valueOf(
                Util.loadConfigAssets(mApp, "news_config.properties").getProperty("CONFIG_NUM_ARTICLES"));


    }


    public static int getRankingOption() {
        return rankingOption;
    }

    public static void setRankingOption(int rankingOption) {
        ReaderController.rankingOption = rankingOption;
    }


    public static ReaderController getInstance(Context app) {
        if( control == null ){
            mApp = app;
            control = new ReaderController();
        }
        return control;
    }


    public static ReaderController getInstance(){
        return getInstance(mApp);
    }

    public static AsyncListView getListView() {
        return listView;
    }

    public static void setListView(AsyncListView listView) {
        ReaderController.listView = listView;
    }

    public static void setModelDistributions(String stringModelDistributions) {
        stringModelDistributions = ModelDistribution.formatModels(stringModelDistributions);
        modelDistributions = Util.fromJson(stringModelDistributions, ModelDistribution.class);
    }


    public static ModelDistribution getModelDistributions() {
        if( modelDistributions == null ){
            modelDistributions = new ModelDistribution();
        }
        return modelDistributions;
    }

    public TextView getUiTVTitle(View v) {
        if( news_title != null ) {
            return (TextView) v.findViewById(news_title);
        }
        return (TextView) v.findViewById(R.id.news_title);
    }

    public TextView getUiTVSummary(View v) {
        if( news_summary != null ) {
            return (TextView) v.findViewById(news_summary);
        }
        return (TextView) v.findViewById(R.id.news_summary);
    }

    public TextView getUiTVRank(View v) {
        if( news_rank  != null ) {
            return (TextView) v.findViewById(news_rank);
        }
        return (TextView) v.findViewById(R.id.news_rank);
    }

    public TextView getUiTVReason(View v) {
        if( news_reason != null ) {
            return (TextView) v.findViewById(news_reason);
        }
        return (TextView) v.findViewById(R.id.news_reason);
    }


    public EditText getUiTvComments(View v) {
        if( news_comments != null ) {
            return (EditText) v.findViewById(news_comments);
        }
        return (EditText) v.findViewById(R.id.news_comments);
    }

    public TextView getUiTVScore(View v) {
        if( news_score != null ) {
            return (TextView) v.findViewById(news_score);
        }
        return (TextView) v.findViewById(R.id.news_score);
    }

    public TextView getUiTVPublisher(View v) {
        if( news_publisher != null ) {
            return (TextView) v.findViewById(news_publisher);
        }
        return (TextView) v.findViewById(R.id.news_publisher);
    }

    public TextView getUiTVFeat(View v) {
        if( news_feat != null ){
            return (TextView)v.findViewById( news_feat);
        }
        return (TextView) v.findViewById(R.id.news_feat);
    }

    public TextView getUiTVFeat2(View v) {
        if( news_feat2 != null ){
            return (TextView)v.findViewById( news_feat2);
        }
        return (TextView) v.findViewById(R.id.news_feat2);
    }

    public ImageView getUiIVImg(View v) {
        if( news_img != null ){
            return (ImageView)v.findViewById( news_img);
        }
        return (ImageView) v.findViewById(R.id.news_img);
    }

    public ImageButton getUiIBShareFb(View v) {
        if( news_btnShareFb != null ){
            return (ImageButton)v.findViewById( news_btnShareFb);
        }
        return (ImageButton) v.findViewById(R.id.news_btnShareFb);
    }

    public ImageButton getUiIBShareTwitter(View v) {
        if( news_btnShareTwitter != null ){
            return (ImageButton)v.findViewById( news_btnShareTwitter);
        }
        return (ImageButton) v.findViewById(R.id.news_btnShareTwitter);
    }

    public ImageButton getUiIBShareTumblr(View v) {
        if( news_btnShareTumblr != null ){
            return (ImageButton)v.findViewById( news_btnShareTumblr);
        }
        return (ImageButton) v.findViewById(R.id.news_btnShareTumblr);
    }

    public ImageButton getUiIBShareMore(View v) {
        if( news_btnShareMore != null ){
            return (ImageButton)v.findViewById( news_btnShareMore);
        }
        return (ImageButton) v.findViewById(R.id.news_btnShareMore);
    }

    public ImageButton getUiIBDislike(View v) {
        if( news_btnDislike != null ){
            return (ImageButton)v.findViewById( news_btnDislike);
        }
        return (ImageButton) v.findViewById(R.id.news_btnDislike);
    }

    public ImageButton getUiIBLike(View v) {
        if( news_btnLike != null ){
            return (ImageButton)v.findViewById( news_btnLike);
        }
        return (ImageButton) v.findViewById(R.id.news_btnLike);
    }

    public Integer getLandscapeLayout(){
        return ( landscape_layout != null )? landscape_layout : R.layout.news_list_item_flat;
    }

    public Integer getPortraitLayout(){
        return ( portrait_layout != null )? portrait_layout : R.layout.news_list_item;
    }

    public ShareHelper getShareHelper(){
        return mShareHelper;
    }

    public NewsArticleVector getmNewsModified() {
        return mNewsModified;
    }

    public void setmNewsModified ( NewsArticleVector newList ){ this.mNewsModified = newList; }

    public DataHandler getDataHandler()
    {
        return mDataHandler;
    }

    public NewsHandler getUIHandler()
    {
        return mNewsHandler;
    }

    public static Context get()
    {
        return mApp;
    }

    public CookieStore getCookieStore()
    {
        return mCs;
    }

    public boolean isConnected() {
        return bIsConnected;
    }

    public void setConnected(boolean bIsConnected) {
        this.bIsConnected = bIsConnected;
    }


//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        I13N.get().log(new Event(this.getClass().getSimpleName(), "onConfigurationChanged"));
//    }

    public void registerUIHandler(NewsHandler uiHandler) {
        mNewsHandler = uiHandler;
    }

    public Config getConfig(){
        return mConfig;
    }

    public void initialize(ReaderMainActivity readerMainActivity, Bundle savedInstanceState, final int messageId) {
        if( readerMainActivity != null ){
            activity = readerMainActivity;
            //getting unique id for device
            if( deviceId == null ) {
                deviceId = android.provider.Settings.Secure.getString( activity.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }

            if( mNewsHandler instanceof QueryNewsHandler ) {
                tempHandler = (QueryNewsHandler) mNewsHandler;
            }
            mNewsHandler = new UIHandler( readerMainActivity );
            I13N.getNew().registerSession( readerMainActivity );
            getDrawerManager(readerMainActivity).onCreateDrawer(savedInstanceState);
        }else{
            if( mNewsHandler == null ) {
                mNewsHandler = new QueryNewsHandler();
            }else if( mNewsHandler instanceof  UIHandler){
                mNewsHandler = tempHandler;
            }
            I13N.getNew().registerSession(mApp);
            getDrawerManager(mApp);
        }

        ReaderController.getInstance().registerUIHandler(mNewsHandler);
        ReaderController.getInstance().getDataHandler().registerUiHandler(mNewsHandler);
        //Select/create the first Fragment
        if (savedInstanceState == null)
        {
            mNewsHandler.post(new Runnable() {
                @Override
                public void run() {
                    //implicitly select the first item (which is the default news list)
                    getDrawerManager(null).selectItem(DrawerManager.DRAWER_DEFAULT, messageId);
                }

            });
        }
        isInitialized = true;
    }

    public static DrawerManager getDrawerManager(Context context) {
        if( drawerManagerInstance == null ){
            try {
                Method method = drawerManager.getMethod( "getInstance", Context.class );
                drawerManagerInstance = (DrawerManager) method.invoke( null, context ); //null for static methods
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return drawerManagerInstance;
    }


    public SlingstoneSrc getSlingstone( Context context ) {
        if( slingstoneSrc == null ){
            slingstoneSrc =  SlingstoneSrc.getInstance( context );
        }
        return slingstoneSrc;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserProfile getUserProfile( ) {
        return userProfile;
    }

    public void setFilters(ArrayList<FilterVO> filters) {
        this.filters = filters;
    }

    public void setNewsSize(int newsSize) {
        this.newsSize = newsSize;
    }

    public int getNewsSize(){
        return this.newsSize;
    }

    public Integer getCurrentArticle() {
        return currentArticle;
    }

    public void setCurrentArticle( Integer position ) {
        currentArticle = position;
    }




    public static boolean isFlagRefreshAsyncListView() {
        return flagRefreshAsyncListView;
    }

    public static void setFlagRefreshAsyncListView(boolean flagRefreshAsyncListView) {
        ReaderController.flagRefreshAsyncListView = flagRefreshAsyncListView;
    }

    public void setDrawerManager(Class<? extends Object> drawManager) {
        drawerManager = drawManager;
    }

    public void clear() {
        drawerManagerInstance = null;
        //mLastReload = null;
    }

    public class Config{

        public String getString(String key)
        {
            return (String) mProp.get(key);
        }

        public void loadConfig() {
            AssetManager am = mApp.getAssets();
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            try {
                br = new BufferedReader(new InputStreamReader(am.open("news_config.xml")));
                String line;
                while ( (line = br.readLine()) != null)
                {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                try {
                    if (br != null)
                        br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            JSONParser parser = new JSONParser();
            JSONObject jobj;
            try {
                jobj = (JSONObject) parser.parse(sb.toString());
                for (Object key : jobj.keySet())
                {
                    mProp.put(key, jobj.get(key));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }
    public Settings getSettings(){
        return mSettings;
    }

    Properties mProp = new Properties();


    //Configurable SharedPreferences
    public class Settings{

        private SharedPreferences mPref;
        private String mI13nEnabledKey;
        private String mLocEnabledKey;
        private String mFlipViewEnabledKey;

        public Settings(){
            mI13nEnabledKey = mApp.getResources().getString(R.string.news_i13nEnabled);
            mLocEnabledKey = mApp.getResources().getString(R.string.news_trackLocEnabled);
            mFlipViewEnabledKey = mApp.getResources().getString(R.string.news_flipViewEnabled);
            mPref = PreferenceManager.getDefaultSharedPreferences(mApp);
        }

        public boolean getI13NEnabled(){
            return mPref.getBoolean(mI13nEnabledKey, true);
        }

        public boolean getLocTrackerEnabled(){
            return mPref.getBoolean(mLocEnabledKey, true);
        }

        public void setI13NEnabled(boolean b) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putBoolean(mI13nEnabledKey, b);
            editor.commit();
        }

        public boolean isFlipViewEnabled() {
            return mPref.getBoolean(mFlipViewEnabledKey, false);
        }
    }

    /**
     * reload the list of news articles? true: query slingstone
     * @return
     */
    public boolean isReload() {
       if( mLastReload == null ){
           return true;
       }else{
           return System.currentTimeMillis() - mLastReload > mRefreshInterval;
       }
    }

    public static void setmRefreshInterval(Long mReaderInterval) {
        ReaderController.mRefreshInterval = mReaderInterval;
    }

    public static void setmLastReload(Long mLastReload) {
        ReaderController.mLastReload = mLastReload;
    }

    public static SettableFuture<NewsArticleVector> createNewsFuture(){
        future =  SettableFuture.create();
        return future;
    }

    public static SettableFuture<NewsArticleVector> getNewsFuture(){
        return future;
    }

    public static void setNewsFuture( SettableFuture<NewsArticleVector> fut ){
        future.cancel( true );
        future = fut;
    }

    public void setMapDrawNewsItem( Integer idx, Drawable drawable ){
        mapDrawNewsItem.put( idx, drawable );
    }

    public Drawable getDrawableNewsItem( Integer idx ){
        return mapDrawNewsItem.get( idx );
    }


    /**
     * Apply filters over a list of news articles
     * @param list if null, it uses the NewsArticleVector instance, otherwise it uses list
     * @return
     */
    public ArrayList applyFilters(ArrayList list){ //, boolean clone
        if( list == null ){
            list = NewsArticleVector.getInstance();
        }
        if( typeOfFilter.equals("AND") ){
            return applyANDFilters(list); //, clone
        }else if( typeOfFilter.equals("OR") ){
            return applyORFilters(list);  //, clone
        }
        return null;
    }

    public ArrayList applyANDFilters(ArrayList list){//, boolean clone
        if (list == null) {
            return list;
        }
//            if (clone) {
//                list = Util.cloneList(list);
//            }
        if (filters != null && !filters.isEmpty() && !list.isEmpty()) {
            Iterator<NewsArticle> it = list.iterator();
            ArrayList<NewsArticle> removeThis = new ArrayList();
            while (it.hasNext()) {
                NewsArticle item = it.next();
                for (FilterVO filter : filters) {
                    if ( (Boolean) filter.validate( item ) ) {
                        removeThis.add( item );
                    }
                }
            }
            for( NewsArticle article : removeThis ){
                list.remove( article );
            }
        }
        return list;
    }


    public ArrayList applyORFilters(ArrayList list){ //, boolean clone){
        synchronized ( list ) {
            if (list == null) {
                return null;
            }

//            if (clone) {
//                list = Util.cloneList(list);
//            }
            Iterator<NewsArticle> it = list.iterator();
            while (it.hasNext()) {
                boolean remove = true;
                NewsArticle item = it.next();
                if (filters != null && !filters.isEmpty() && !list.isEmpty()) {
                    for (FilterVO filter : filters) {
                        if ( !(Boolean) filter.validate( item ) ) {
                            remove = false;
                            break;
                        }
                    }
                }
                if (remove) {
                    Log.e("", "Removing item: " + item.getIdx() );
                    it.remove();
                }
            }
            NewsArticleVector.setFilteredInstance( list );
            return list;
        }
    }


    public void clearFilters() {
        if( filters != null ){
            filters.clear();
        }
        filters = null;
    }


    public Integer getNews_btnShareFb() {
        return news_btnShareFb;
    }

    public void setNews_btnShareFb(Integer news_btnShareFb) {
        if (news_btnShareFb != null) {
            this.news_btnShareFb = news_btnShareFb;
        } else {
            this.news_btnShareFb = R.id.news_btnShareFb;
        }
    }

    public Integer getNews_btnShareTwitter() {
        return news_btnShareTwitter;
    }

    public void setNews_btnShareTwitter(Integer news_btnShareTwitter) {
        if (news_btnShareTwitter != null) {
            this.news_btnShareTwitter = news_btnShareTwitter;
        } else {
            this.news_btnShareTwitter = R.id.news_btnShareTwitter;
        }
    }

    public Integer getNews_btnShareTumblr() {
        return news_btnShareTumblr;
    }

    public void setNews_btnShareTumblr(Integer news_btnShareTumblr) {
        if (news_btnShareTumblr != null) {
            this.news_btnShareTumblr = news_btnShareTumblr;
        } else {
            this.news_btnShareTumblr = R.id.news_btnShareTumblr;
        }
    }

    public Integer getNews_btnShareMore() {
        return news_btnShareMore;
    }

    public void setNews_btnShareMore(Integer news_btnShareMore) {
        if (news_btnShareMore != null) {
            this.news_btnShareMore = news_btnShareMore;
        } else {
            this.news_btnShareMore = R.id.news_btnShareMore;
        }
    }

    public Integer getNews_btnLike() {
        return news_btnLike;
    }

    public void setNews_btnLike(Integer news_btnLike) {
        if (news_btnLike != null) {
            this.news_btnLike = news_btnLike;
        } else {
            this.news_btnLike = R.id.news_btnLike;
        }
    }

    public Integer getNews_btnDislike() {
        return news_btnDislike;
    }

    public void setNews_btnDislike(Integer news_btnDislike) {
        if (news_btnDislike != null) {
            this.news_btnDislike = news_btnDislike;
        } else {
            this.news_btnDislike = R.id.news_btnDislike;
        }
    }

    public String[] requestEmailKeywords(HashMap<String, Object> payload){
        String url, result;
        url = Util.loadConfigAssets(mApp, Constants.CONFIG_NEWS_PROPERTIES).
                getProperty(Constants.CONFIG_NEWS_FILTERED_BY_EMAIL);
        result = HttpController.getHttpGetResponse(url, payload);
        //result = HttpController.getHttpGetResponse(url, payload, null, 0 );
        return result.split(" ");
    }


    public void requestPersonalization(HashMap<String, Object> payload){
        String url, result;
        int endPos;

        // Communication with Emma's server
        List<NewsArticle> tempList = null;
        if( rankingOption == 1 || rankingOption == 3 ) {
            // re-ranked news articles
            url = Util.loadConfigAssets(mApp, Constants.CONFIG_NEWS_PROPERTIES).
                    getProperty(Constants.CONFIG_PERSONALIZATION_MULTIBANDIT_LEARNING);
            result = HttpController.getHttpPostResponse(url, payload, null, 0);
            NewsArticleVector.setList1(NewsArticleVector.fromJson(result, false));
            tempList = NewsArticleVector.getList1();

            // model distributions
            String urlModelsDistributions = Util.loadConfigAssets(ReaderController.get()
                    , Constants.CONFIG_NEWS_PROPERTIES)
                    .getProperty(Constants.CONFIG_NEWS_MODELS_DISTRIBUTION);
            ReaderController.setModelDistributions(HttpController
                    .getHttpGetResponse(urlModelsDistributions, null));
        }
        // Communication with William's server
        if( rankingOption == 2 || rankingOption == 3 ) {
            url = Util.loadConfigAssets(mApp, Constants.CONFIG_NEWS_PROPERTIES).
                    getProperty(Constants.CONFIG_PERSONALIZATION_LEARNING_FROM_ADVISE);
            result = HttpController.getHttpPostResponse(url, payload, null, 0);
            NewsArticleVector.setList2(NewsArticleVector.fromJson(result, false));
            tempList = NewsArticleVector.getList2();
        }

        if( rankingOption == 3 ) {
            tempList = NewsArticleVector.mergeLists();
        }
        endPos = tempList.size();
        NewsArticleVector.sortNews( tempList );
        NewsArticleVector.setIsRecommendedBy( tempList );
        NewsArticleVector.increaseEndPosBatch(endPos);

        sendRefreshNewsEvent();
    }


    public static void loadImages(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getDrawerManager(null).selectItem(0, -1);
            }
        });
    }


    public void sendFeedback( String list1, String list2){
        HashMap<String, Object> payload = new HashMap();
        payload.put( "user_info",  Util.toJson( userProfile) );
        payload.put( "device_id",  deviceId );

        String url;
        // Emma's server
        if( list1 != null ) {
            payload.put( "news_feedback", list1);
            url = Util.loadConfigAssets(mApp, Constants.CONFIG_NEWS_PROPERTIES).
                    getProperty(Constants.CONFIG_FEEDBACK_MULTIBANDIT_LEARNING);
            HttpController.getHttpPostResponse(url, payload, null, 0);
        }

        // William's server
        if( list2 != null ) {
            payload.put("news_feedback", list2);
            url = Util.loadConfigAssets(mApp, Constants.CONFIG_NEWS_PROPERTIES).
                    getProperty(Constants.CONFIG_FEEDBACK_LEARNING_FROM_ADVISE);
            HttpController.getHttpPostResponse(url, payload, null, 0);
        }
    }

    public void sendFeedbackTask(){
        NewsHttpGetTask task = new NewsHttpGetTask( Constants.CONFIG_ID_FEEDBACK );
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                NewsArticleVector.getArticlesWithFeedback(NewsArticleVector.getList1()),
                NewsArticleVector.getArticlesWithFeedback(NewsArticleVector.getList2()));
    }

    public void requestPersonalizationTask(){
        NewsHttpGetTask task = new NewsHttpGetTask( Constants.CONFIG_ID_PERSONALIZATION );
        HashMap payload = ReaderController.getInstance().createPersonalizationRequest();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payload);
    }


    public HashMap<String, String> createPersonalizationRequest(){
        HashMap<String, String> map = new HashMap<>();
        map.put( "news", Util.toJson( NewsArticleVector.getInstance().getRemaining() ));
        map.put( "user_info",  Util.toJson( userProfile) );
        map.put( "num_articles", "" + NewsArticleVector.getBatchArticlesToShow() );
        map.put( "device_id",  deviceId );
        return map;
    }


    private class NewsHttpGetTask extends AsyncTask<Object, Void, Void> {
        private String id;

        public NewsHttpGetTask(String id){
            this.id = id;
        }


        @Override
        protected Void doInBackground(Object... params) {
            if( id.equals( Constants.CONFIG_ID_PERSONALIZATION ) ){
                requestPersonalization((HashMap) params[0]);
            } else if( id.equals( Constants.CONFIG_ID_FEEDBACK ) ){
                sendFeedback( (String) params[0], (String) params[1] );
                NewsArticleVector.resetVisitedArt();
            }
            return null;
        }
    }

    public static void sendRefreshNewsEvent() {
        RefreshNewsListEvent event = new RefreshNewsListEvent();
        NewsArticleVector vector  = NewsArticleVector.getArticlesPerView();
        event.setArticleList(vector);
        NewsResponseEvent event1 = new NewsResponseEvent();
        event1.setNews( vector );

        EventBus.getDefault().post(event);
        if( activity != null ) {
            loadImages();
        }
    }
}
