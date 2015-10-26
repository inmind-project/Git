package com.yahoo.inmind.services.news.model.slingstone;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.services.news.control.cache.ImgLruCacher;
import com.yahoo.inmind.services.news.model.events.RequestFetchNewsEvent;
import com.yahoo.inmind.services.news.model.events.ResponseFetchNewsEvent;
import com.yahoo.inmind.services.news.control.i13n.I13N;
import com.yahoo.inmind.services.news.model.i13n.UUIDEvent;
import com.yahoo.inmind.services.news.model.source.AsyncSource;
import com.yahoo.inmind.services.news.model.vo.JsonItem;
import com.yahoo.inmind.services.news.model.vo.NewsArticle;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.control.util.JsonUtil;
import com.yahoo.inmind.services.news.control.util.WebImgUtil;
import com.yahoo.inmind.services.news.view.reader.DrawerItem;
import com.yahoo.inmind.services.news.view.reader.DrawerManager;
import com.yahoo.inmind.services.news.view.slingstone.SlingstoneRenderer;

import de.greenrobot.event.EventBus;

public class SlingstoneSrc extends AsyncSource<NewsArticle> {

    //Constants for constructing the url for retrieving the JsonUtil
    private static final String p13nProto = "http";
    private static final String p13nHost = "any-ts.cpu.yahoo.com:4080";
    private static final String p13nPath = "/score/v9/homerun/en-US/unified/ga";
    private static final String p13nParam = "debug=true&today.region=remove&Cookie=cookiejar&snippet=true&cap_summary=true&snippet_count=10"; //170
    private static final String p13nurl = p13nProto + "://" + p13nHost + p13nPath + "?" + p13nParam;

    private Context mCtx;
    private DrawerManager mDm;
    private ImgLruCacher mCache;
    private WebImgUtil mImgUtil;
    private EventBus bus;
    private ReaderController reader;
    private boolean isInitialize = false;
    private static SlingstoneSrc instance;
    private static boolean loadStoredNews = false;

    public static SlingstoneSrc getInstance( Context context) {
        if (instance == null) {
            instance = new SlingstoneSrc( context );
        }
        return instance;
    }


    private SlingstoneSrc(Context ctx) {
        super();
        this.mCtx = ctx;
        reader = ReaderController.getInstance();
        setURL();
        mImgUtil = new WebImgUtil(ctx);
        bus = EventBus.getDefault();
        bus.register( this );
    }


    private void setURL(){
        String configUrl = reader.getConfig().getString("SS_URL");
        if (configUrl != null) {
            url = configUrl;
        }else{
            url = p13nurl;
        }
    }

    public SlingstoneSrc enableCache(ImgLruCacher cache)
    {
        mCache = cache;
        return this;
    }

    public void onEventAsync( RequestFetchNewsEvent event ){
        isInitialize = event.isInitialize();
        fetchData( new ArrayList<JsonItem>(), event.getMbRequestId() );
    }

    //++Source
    //This function must be executed on the thread other than the UI thread.
    @Override
    public void fetchData(ArrayList<JsonItem> list, int messageId) {
        ArrayList<JsonItem> listTemp = new ArrayList<>();
        boolean reload = reader.isReload();
        //Read cookies if available
        //String identStr = reader.getCookieStore().getCookies();
        if( reader.getmNewsModified() != null ){
            listTemp.addAll(reader.getmNewsModified());
            reader.setmNewsModified(null);
        }else {
            if( reload ) {
                String identStr = reader.getCookieStore().getInMindProfStr();
                setURL();
                JsonUtil jsonUtil = new JsonUtil(url);
                if (identStr == null) {
                    listTemp = jsonUtil.fetch( loadStoredNews );
                    //TODO: for testing purposes
                    generateUserProfile(jsonUtil, true);
                } else {
                    //Unmark below to use Yahoo's profile
                    //jsonUtil.fetch(identStr);//identStr is a String of cookie(s)

                    //Use the 'fake' user profile of Slingstone here.
                    StringBuilder sb = new StringBuilder(jsonUtil.getUrl());
                    sb.append("&");
                    sb.append(Constants.JSON_SS_FAKE_USER_PROFILE_PARAM_NAME);
                    sb.append("=");
                    try {
                        sb.append(URLEncoder.encode(identStr, "UTF-8").replaceAll("%5C", ""));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    jsonUtil.setUrl(sb.toString());
                    listTemp = jsonUtil.fetch( loadStoredNews );
                    generateUserProfile(jsonUtil, false);
                }

                if( !loadStoredNews ) {
                    JSONArray elements = (JSONArray) jsonUtil.getObjByPath(Constants.JSON_YAHOO_COKE_STREAM_ELEMENTS);
                    int numElements = reader.getNewsSize() > elements.size() ? elements.size() : reader.getNewsSize();
                    for (int i = 0; elements != null && i < numElements; i++) {
                        JSONObject jobj = (JSONObject) elements.get(i);
                        NewsArticle arti = new NewsArticle(i, jobj);
                        listTemp.add(arti);
                    }
                }
                NewsArticleVector.replace(listTemp);
                NewsArticleVector.setCopyInstance( listTemp );
                ReaderController.setmLastReload(System.currentTimeMillis());
            }else{
                //listTemp.addAll(NewsArticleVector.getInstance());
                if( reader.isApplyFilter ){
                    NewsArticleVector.setInstance( NewsArticleVector.getFilteredInstance() );
                }else{
                    NewsArticleVector.setInstance( NewsArticleVector.getCopyInstance() );
                }
            }

            if( ReaderController.getRankingOption() != 0 ){
                JsonUtil.fecthPersonalization();
            }
            if( isInitialize ){
                NewsArticleVector.initialize( true );
                isInitialize = false;
            }
            if( messageId != -1) {
                bus.post(new ResponseFetchNewsEvent(messageId));
            }
        }
        if( reader.isApplyFilter && reload ){
            reader.applyFilters( null );
        }
        //update list
        for( JsonItem item : NewsArticleVector.getArticlesPerView() ){
            list.add( item );
        }
        SlingstoneSrc.super.fetchData(list, messageId);//Call super at the end to signal the completion of user profile
        if( reload ) {
            trackUnreadUUIDs(list);
        }
        if (ReaderController.getNewsFuture() != null) {
            ReaderController.getNewsFuture().set( NewsArticleVector.getInstance() );
        }
        reader.sendRefreshNewsEvent();
    }

    /**
     * Send all UUID to the I13N server for tracking unread behaviors.
     * */
    private void trackUnreadUUIDs(ArrayList<JsonItem> list) {
        //1. to JsonUtil
        StringBuilder sb = new StringBuilder("{\"UUIDs\":[");
        int cnt = 0;
        for (JsonItem ji : list)
        {
            if (cnt != 0)
                sb.append(", ");

            //{"2":"o23k12p3-123o1k-234234-234sdg234"},
            //{"3":"er34123d-123fkg-351235-ads231f23"}
            sb.append("{");
            NewsArticle ni = (NewsArticle) ji;
            sb.append("\"uuid\":\"");
            sb.append(ni.getUuid());
            sb.append("\", \"idx\":");
            sb.append(String.valueOf(ni.getIdx()));
            sb.append("}");
            cnt++;
        }
        sb.append("]}");

        //2. Send to I13N
        I13N.get().log(new UUIDEvent(reader.getCookieStore().getCurrentUserName(),
                sb.toString()));
    }

    //Personal data begins here
    private void generateUserProfile(JsonUtil jsonUtil, boolean defaultProf) {
        if( defaultProf == true ){
            //FIXME we need some mock data for testing purposes. Just leave mProfile = null
            UserProfile profile = new UserProfile();
            profile.getDemographics().put(Constants.JSON_USER_AGE, "26");
            profile.getDemographics().put(Constants.JSON_USER_GENDER, "m");
//            profile.getmPosDecWiki().put("Barack_Obama", "0.5");
//            profile.getmPosDecWiki().put("Franklin_D._Roosevelt", "0.5");
//            profile.getmPosDecWiki().put("Detroit_Lions", "0.5");
//            profile.getmPosDecWiki().put("Ben_Roethlisberger", "0.5");
//            profile.getmCapWiki().put("Michael_Jordan","1.97");
//            profile.getmCapWiki().put("Kobe_Bryant","1.38");
//            profile.getmCapYct().put("yct:001000012","2.52");
//            profile.getmNegInfWiki().put("Philadelphia_Eagles","-1.97");
//            profile.getmFbWiki().put("Pirates","2.18");
//            profile.getmFbYct().put("yct:0010010123","1.98");
//            profile.getmNegDecWiki().put("Chicago_White_Sox","-1.23");
//            profile.getmNegDecYct().put("yct:00101011128","-0.32");

            mProfile = profile;
        }else{
            JSONObject jobjUser = (JSONObject) jsonUtil.getObjByPath(Constants.JSON_YAHOO_USER_PROFILE_PATH);
            if (jobjUser != null) {
                UserProfile profile = new UserProfile();
                fillProfile(jobjUser, Constants.JSON_CAP_ENTITY_WIKI, profile.getmCapWiki());
                fillProfile(jobjUser, Constants.JSON_CAP_YCT_ID, profile.getmCapYct());
                fillProfile(jobjUser, Constants.JSON_POSITIVE_DEC_WIKIID, profile.getmPosDecWiki());
                fillProfile(jobjUser, Constants.JSON_POSITIVE_DEC_YCT, profile.getmPosDecYct());
                fillProfile(jobjUser, Constants.JSON_NEGATIVE_DEC_WIKIID, profile.getmNegDecWiki());
                fillProfile(jobjUser, Constants.JSON_NEGATIVE_DEC_YCT, profile.getmNegDecYct());
                fillProfile(jobjUser, Constants.JSON_FB_WIKIID, profile.getmFbWiki());
                fillProfile(jobjUser, Constants.JSON_FB_YCT, profile.getmFbYct());
                fillProfile(jobjUser, Constants.JSON_NEGATIVE_INF_WIKIID, profile.getmNegInfWiki());
                fillProfile(jobjUser, Constants.JSON_NEGATIVE_INF_YCT, profile.getmNegInfYct());
                //fillProfile(jobjUser, JSON_USER_PROPUSAGE, profile.mUserProp);

                Object tmp = JsonUtil.getProp(jobjUser, Constants.JSON_USER_AGE);
                if (tmp != null)
                    profile.getDemographics().put(Constants.JSON_USER_AGE, (String) tmp);
                tmp = JsonUtil.getProp(jobjUser, Constants.JSON_USER_GENDER);
                if (tmp != null)
                    profile.getDemographics().put(Constants.JSON_USER_GENDER, (String) tmp);
                mProfile = profile;
                //			for (Entry<String, String> ent : mProfile.mCapWiki.entrySet())
                //				Log.i("inmind", "key:" + ent.getKey() + ", val: " + ent.getValue());

            }else{
                mProfile = null;
            }
        }
        reader.setUserProfile((UserProfile) mProfile);
    }

    //Data Thread
    @Override
    public void generateItemsFromProfile() {
        mDm.prepareForExtension();
        if (mProfile != null)
        {
            UserProfile profile = (UserProfile) mProfile;
            //Here we simply add CAP_ENTITIES_WIKI as filters into the drawer.
            for (Entry<String, String> ent : profile.getmPosDecWiki().entrySet())
            {
                DrawerItem item = new DrawerItem();
                item.name = ent.getKey() + ":" + ent.getValue();
                item.srcs.add(SlingstoneSrc.getInstance(mCtx).enableCache(mCache).filter("&filter=(JSON_CAP_ENTITY_WIKI=" + ent.getKey() + ")"));
                item.renderers.add(new SlingstoneRenderer(mCtx).enableCache(mCache));
                mDm.addItem(item);
            }
        }
    }

    //UI Thread
    @Override
    public void showExtendedOptions() {
        mDm.postItemNamesToDrawer();
    }
    //--Source

    //++AsyncSource
    @Override
    /**
     * @return True pass it to loadItemInParallel(); False otherwise.
     * */
    protected boolean filterItem(NewsArticle art) {
        if (art == null ||
                (reader.getDrawableNewsItem( art.getIdx() ) != null ||
                        art.getImgPath() != null) || //image is already loaded
                art.getImgUrl() == null)		//image has no url
        {
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadItemInParallel(NewsArticle article1, BitmapFactory.Options options, int msgWhat, Handler uiHandler) {

        boolean flagRefresh = true; //tells the caller that refresh is necessary, since we really load images into memory

        //ojrl
        int size = NewsArticleVector.getInstance().size();
        for( int i = 0; i < size; i++ ) {
            NewsArticle art = NewsArticleVector.getInstance().get( i );
            Bitmap bmp = null;
            boolean flagCacheMemory = false, flagCacheDisk = false;

            if (mCache == null) {//Do not use cache, download images & load into memory directly
                Log.e("DEBUG", "title: " + art.getTitle());
                reader.setMapDrawNewsItem(art.getIdx(), mImgUtil.getDrawableFromUrl(art.getImgUrl(), options));
                Drawable drawable = reader.getDrawableNewsItem(art.getIdx());
                if (drawable != null &&
                        drawable.getIntrinsicWidth() >= drawable.getIntrinsicHeight()) {
                    art.setDimension(NewsArticle.DIM_LANDSCAPE);
                } else {
                    art.setDimension(NewsArticle.DIM_PORTRAIT);
                }
            } else {
                if( art.getImgPath() != null ) {
                    // get the image from cache
                    bmp = mCache.get( art.getImgPath() );
                    // get the image from the disk
                    if (bmp == null) {
                        bmp = mImgUtil.readBmp(art.getImgPath());
                        if ( bmp == null ) {
                            flagCacheDisk = true;
                            //Log.e("DEBUG", "Image in disk" );
                        }
                    }else{
                        //Log.e("DEBUG", "Image in mCache" );
                        flagCacheMemory = true;
                        flagCacheDisk = true;
                    }
                }

                //1. Download the image if it is not in the cache nor in the disk
                if( bmp == null && art.getImgUrl() != null ) {
                    bmp = mImgUtil.bmpFromUrl(art.getImgUrl(), 0, 0, options);
                }

                // article doesn't have an image associated to it
                if (bmp == null ) {
                    flagRefresh = false;
                } else {
                    if (bmp.getWidth() >= bmp.getHeight())
                        art.setDimension(NewsArticle.DIM_LANDSCAPE);
                    else
                        art.setDimension(NewsArticle.DIM_PORTRAIT);

                    String bmpExportPath = ImgLruCacher.IMG_CACHE_PATH + art.getUuid();
                    //2. Cache to disk
                    if ( !flagCacheDisk && mImgUtil.writeBmp(bmp, bmpExportPath) ) {
                        art.setImgPath(bmpExportPath);
                        //Log.e("DEBUG", "Caching article in disk: " + art.getTitle() + "  hashcode: " + this.hashCode());
                    }

                    //3. Cache to memory when cache is still ample
                    if ( mCache.isAmple() ) {
                        if( !flagCacheMemory ) {
                            mCache.put(art.getImgPath(), bmp);
                            //Log.e("DEBUG", "Caching article in memory: " + art.getTitle() + "  hashcode: " + this.hashCode());
                        }
                    } else {
                        //Log.e("DEBUG", "recycling" );
                        //bmp.recycle();
                    }
                }
            }

            if( flagRefresh && art.getTitle().equals( art.getTitle() ) ){
                //Log.e("DEBUG","Notifying UI");
                Message msg = new Message();
                msg.what = msgWhat;
                msg.obj = art;
                uiHandler.sendMessage(msg); //Send msg to notify UI to update
            }
            flagRefresh = true;
        }
        return true;
    }
    //--AsyncSource

    public void setDrawerManager(DrawerManager drawerManager) {
        mDm = drawerManager;
    }
}
