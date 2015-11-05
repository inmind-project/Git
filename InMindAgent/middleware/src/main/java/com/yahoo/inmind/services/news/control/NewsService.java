package com.yahoo.inmind.services.news.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.yahoo.inmind.comm.generic.model.MBRequest;
import com.yahoo.inmind.comm.news.model.FilterByEmailEvent;
import com.yahoo.inmind.comm.news.model.NewsResponseEvent;
import com.yahoo.inmind.comm.news.model.NewsUpdateEvent;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.services.generic.control.GenericService;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.model.events.ExpandArticleEvent;
import com.yahoo.inmind.services.news.model.events.GoToArticleEvent;
import com.yahoo.inmind.services.news.model.events.RequestFetchNewsEvent;
import com.yahoo.inmind.services.news.model.slingstone.ModelDistribution;
import com.yahoo.inmind.services.news.model.slingstone.UserProfile;
import com.yahoo.inmind.services.news.model.vo.FilterVO;
import com.yahoo.inmind.services.news.model.vo.NewsArticle;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;
import com.yahoo.inmind.services.news.view.browser.BaseBrowser;
import com.yahoo.inmind.services.news.view.browser.LoginBrowser;
import com.yahoo.inmind.services.news.view.reader.ReaderMainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsService extends GenericService {

    private static long mUpdateTime;
    private static long mRefreshInterval; //seconds
    private static boolean isTimerActivated = false;
    /**
     * Concurrency
     */
    private final int MAX_THREADS = 10;
    private Properties properties;
    private ExecutorService executorService;
    private ListeningExecutorService pool;
    /**
     * Controllers
     **/
    private ReaderController reader;
    /**
     * update news automatically
     **/
    private Timer timer;
    private NewsArticle firstItem;


    // ****************************** SERVICE'S LIFE CYCLE *****************************************

    public NewsService() {
        super( null );
    }

    public static void setRefreshTime(long refreshTime) {
        mRefreshInterval = refreshTime;
        ReaderController.getInstance().setmRefreshInterval(mRefreshInterval);
    }

    public static void setmUpdateTime(long mUpdateTime) {
        NewsService.mUpdateTime = mUpdateTime;
        isTimerActivated = false;
    }

    public static void setNewsListSize(int size) {
        ReaderController.getInstance().setNewsSize(size);
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return mBinder;
    }

    @Override
    public void doAfterBind() {
        reader = ReaderController.getInstance(mContext);
        initialize(getApplicationContext());
        //FIXME: load initial set of news is required?
//        MBRequest request = MBRequest.build( Constants.MSG_GET_NEWS_ITEMS);
//        request.put( Constants.FLAG_SEND_EVENT, false );
//        getNewsItems( request );
    }

    // ****************************** GETTERS AND SETTERS ******************************************

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        executorService.shutdown();
        pool.shutdown();
    }

    public void initialize(Context app) {
        try {
            reader.getSlingstone(getApplicationContext());
            executorService = Executors.newFixedThreadPool(MAX_THREADS);
            pool = MoreExecutors.listeningDecorator(executorService);
            requests = new HashMap<>();
            timer = new Timer();
            properties = Util.loadConfigAssets(app, "midd_config.properties");
            String value = properties.getProperty("newsRefreshTime");
            mRefreshInterval = value == null ? null : Long.parseLong(value);
            ReaderController.setmRefreshInterval(mRefreshInterval);
            value = properties.getProperty("newsUpdateTime");
            mUpdateTime = value == null ? null : Long.parseLong(value);
        } catch (Exception e) {
            //nothing
        }
    }


    // ****************************** SERVICE CALLS ************************************************

    public void release() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
    }

    /***
     * This method retrieves the most recent list of news articles. First it tries to get it from the
     * static context and if the list is empty it tries to get it from the the NewsResponseEvent sticky
     * event. Then, applies the corresponding filter (if ones)
     *
     * @param request
     * @return
     */
    public NewsArticleVector getNewsItems(MBRequest request) {
        NewsArticleVector list = Util.cloneList(NewsArticleVector.getInstance());
        if (list.isEmpty() && mb.getStickyEvent(new NewsResponseEvent().getClass()) != null) {
            list = mb.getStickyEvent(new NewsResponseEvent().getClass()).getNews();
        }
        if (list.isEmpty()) {
            requestNews(request);
            try {
                list = Util.cloneList(ReaderController.getNewsFuture().get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        reader.setFilters((ArrayList<FilterVO>) request.get(Constants.BUNDLE_FILTERS));
        reader.applyFilters(list); //, false
        return list;
    }

    /**
     * This method is manifold purposes: it retrieves the list of news articles and then shows it either in
     * the predefine news reader activity or in a customized activity
     *
     * @param request
     */
    public void startNewsActivity(MBRequest request) {
        Boolean clearFilters = (Boolean) request.get(Constants.BUNDLE_CLEAR_FILTERS);
        if (clearFilters == null
                || (clearFilters != null && clearFilters == true)) {
            reader.clearFilters();
        }
        NewsArticleVector.release();
        //NewsArticleVector.getInstance().clear();

        try {
            if (request.get(Constants.BUNDLE_MODIFIED_NEWS) != null) {
                reader.setmNewsModified((NewsArticleVector) request.get(Constants.BUNDLE_MODIFIED_NEWS));
            }
            if (request.get(Constants.BUNDLE_DRAWER_MANAGER) != null) {
                reader.setDrawerManager(Class.forName((String) request.get(Constants.BUNDLE_DRAWER_MANAGER)));
            }

            Class clazz = null;
            if (request.get(Constants.BUNDLE_ACTIVITY_NAME) == null) {
                clazz = ReaderMainActivity.class;
            } else {
                try {
                    clazz = Class.forName((String) request.get(Constants.BUNDLE_ACTIVITY_NAME));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(mContext, clazz);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.UI_LANDSCAPE_LAYOUT, (Integer) request.get(Constants.UI_LANDSCAPE_LAYOUT));
            intent.putExtra(Constants.UI_PORTRAIT_LAYOUT, (Integer) request.get(Constants.UI_PORTRAIT_LAYOUT));
            intent.putExtra(Constants.UI_NEWS_RANK, (Integer) request.get(Constants.UI_NEWS_RANK));
            intent.putExtra(Constants.UI_NEWS_TITLE, (Integer) request.get(Constants.UI_NEWS_TITLE));
            intent.putExtra(Constants.UI_NEWS_SCORE, (Integer) request.get(Constants.UI_NEWS_SCORE));
            intent.putExtra(Constants.UI_NEWS_SUMMARY, (Integer) request.get(Constants.UI_NEWS_SUMMARY));
            intent.putExtra(Constants.UI_NEWS_FEAT, (Integer) request.get(Constants.UI_NEWS_FEAT));
            intent.putExtra(Constants.UI_NEWS_FEAT2, (Integer) request.get(Constants.UI_NEWS_FEAT2));
            intent.putExtra(Constants.UI_NEWS_PUBLISHER, (Integer) request.get(Constants.UI_NEWS_PUBLISHER));
            intent.putExtra(Constants.UI_NEWS_REASON, (Integer) request.get(Constants.UI_NEWS_REASON));
            intent.putExtra(Constants.UI_NEWS_IMG, (Integer) request.get(Constants.UI_NEWS_IMG));
            intent.putExtra(Constants.UI_NEWS_SHARE_FB, (Integer) request.get(Constants.UI_NEWS_SHARE_FB));
            intent.putExtra(Constants.UI_NEWS_SHARE_TWITTER, (Integer) request.get(Constants.UI_NEWS_SHARE_TWITTER));
            intent.putExtra(Constants.UI_NEWS_SHARE_TMBLR, (Integer) request.get(Constants.UI_NEWS_SHARE_TMBLR));
            intent.putExtra(Constants.UI_NEWS_SHARE_MORE, (Integer) request.get(Constants.UI_NEWS_SHARE_MORE));
            intent.putExtra(Constants.FLAG_REFRESH, (Boolean) request.get(Constants.FLAG_REFRESH));

            if (request.get(Constants.BUNDLE_FILTERS) != null) {
                reader.setFilters((ArrayList<FilterVO>) request.get(Constants.BUNDLE_FILTERS));
                reader.isApplyFilter = true;
            } else {
                reader.isApplyFilter = false;
                reader.clearFilters();
            }
            if (request.get(Constants.CONFIG_NEWS_RANKING_OPTION) != null) {
                ReaderController.setRankingOption((Integer) request.get(Constants.CONFIG_NEWS_RANKING_OPTION));
            }
            if (request.get(Constants.BUNDLE_TYPE_FILTER) != null) {
                if (request.get(Constants.BUNDLE_TYPE_FILTER).equals("AND")) {
                    ReaderController.typeOfFilter = "AND";
                } else {
                    ReaderController.typeOfFilter = "OR";
                }
            }

            checkTimer();
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void filterNewsByEmail(MBRequest request) {
        mb.send(NewsService.this, new FilterByEmailEvent(request));
    }

    public void onEventAsync(FilterByEmailEvent event) {
        //TODO: do we need a payload here?
        HashMap<String, Object> payload = null;
        reader.isApplyFilter = true;
        ReaderController.typeOfFilter = "OR";
        //TODO: uncomment this line when communication with server is working
        String[] categories = reader.requestEmailKeywords(payload);
        ArrayList<FilterVO> filters = new ArrayList<>();
        MBRequest request = event.getMbRequest();
        for (String category : categories) {
            filters.add( new FilterVO( Constants.ARTICLE_CATEGORIES, Constants.OPERATOR_EQUALS_TO,
                    category ) );
        }
        request.put(Constants.BUNDLE_FILTERS, filters);
        //request.put( Constants.BUNDLE_MODIFIED_NEWS, loadNewsItems(request) );
        request.put(Constants.BUNDLE_TYPE_FILTER, "OR");
        request.put(Constants.BUNDLE_CLEAR_FILTERS, false);
        startNewsActivity(request);
    }

    /**
     * This method retrieves a list of news articles (the result is gotten in the event handler
     * onEvent( NewsResponseEvent event). If the current list of news articles is not updated then
     * it requests an update to the yahoo's news server (slingstone) otherwise returns the current
     * list of news articles.
     *
     * @param request
     */
    public NewsArticleVector loadNewsItems(MBRequest request) {
        NewsArticleVector vector;
        if (request.get(Constants.BUNDLE_FILTERS) != null
                && ((ArrayList) request.get(Constants.BUNDLE_FILTERS)).isEmpty() == false) {
            reader.isApplyFilter = true;
        }
        requests.put(request.hashCode(), request);
        if (reader.isReload() || (request.get(Constants.FLAG_FORCE_RELOAD) != null &&
                (Boolean) request.get(Constants.FLAG_FORCE_RELOAD))) {
            vector = requestNews(request);
        } else {
            vector = getNewsItemList(request.hashCode(), request);
        }
        checkTimer();
        return vector;
    }

    public UserProfile requestUserProfile(MBRequest request) {
        return reader.getUserProfile();
    }

    private NewsArticleVector requestNews(MBRequest request) {
        NewsArticleVector vector = null;
        if (reader.isInitialized) {
            RequestFetchNewsEvent event = new RequestFetchNewsEvent(request.hashCode());
            event.setArticleId((Integer) request.get(Constants.BUNDLE_ARTICLE_ID));
            mb.send(NewsService.this, event);
        } else {
            ReaderController.createNewsFuture();
            pool.submit(new NewsAsyncFunction(request));
            try {
                vector = ReaderController.getNewsFuture().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return vector;
    }

    /**
     * This method retrieves the most recent list of news articles and send it to the UI through
     * the NewsResponseEvent event
     *
     * @param messageId
     * @param request
     */
    public NewsArticleVector getNewsItemList(int messageId, MBRequest request) {
        MBRequest req = requests.remove(messageId);
        if (req != null) {
            NewsArticleVector list = getNewsItems(request);
            // we don't want to send the response event when we are not waiting for a response,
            // otherwise events would be handled by the consumer without previous request
            Boolean shouldSend = (Boolean) req.get(Constants.FLAG_SEND_EVENT);
            if ((shouldSend == null) || (shouldSend != null && shouldSend == true)) {
                NewsResponseEvent event;
                if (list != null && !list.isEmpty()) {
                    if (req.get(Constants.FLAG_RETURN_JSON) != null &&
                            (Boolean) req.get(Constants.FLAG_RETURN_JSON) == true) {
                        event = new NewsResponseEvent((Integer) req.get(Constants.QUALIFIER_NEWS),
                                list.toJson());
                    } else {
                        Integer qualifier = req.get(Constants.QUALIFIER_NEWS) == null ? 0
                                : (Integer) req.get(Constants.QUALIFIER_NEWS);
                        event = new NewsResponseEvent(qualifier, list);
                    }
                    firstItem = list.get(0);
                } else {
                    event = new NewsResponseEvent();
                }
                event.setMbRequestId(request.hashCode());
                mb.postSticky(event); //Send msg to notify UI to updatelist
            }
            return list;
        }
        return null;
    }

    /**
     * This method sends a news update notification to the UI. This is activated when the service
     * identified a change on the list of news in the server
     */
    public void getNewsUpdate(MBRequest request) {
        //request.setRequestId( Constants.MSG_GET_NEWS_ITEMS );
        NewsArticleVector list = getNewsItems(request);
        if (firstItem != null && firstItem.getTitle().equals(list.get(0).getTitle()) == false) {
            NewsUpdateEvent event = new NewsUpdateEvent(list);
            firstItem = list.get(0);
            mb.postSticky(event);
        }
    }

    /**
     * This method opens the login activity. The results of this action are retrieved to the bound
     * activity (which comes into the cache memory) by calling the onActivityResult() method.
     *
     * @param request
     */
    public void login(MBRequest request) {
        Intent intent = new Intent(mContext, LoginBrowser.class);
        intent.putExtra(BaseBrowser.LAUCH_BROWSER_URL, LoginBrowser.loginUrl);
        Activity parent = (Activity) request.get(Constants.CONTENT);
        parent.startActivityForResult(intent, (Integer) request.get(Constants.RESULTS_LOGIN));
    }

    public NewsArticleVector applyFilter(MBRequest request, NewsArticleVector newsItems) {
        if (newsItems == null) {
            newsItems = getNewsItems(request);
            if (newsItems == null) {
                return NewsArticleVector.getInstance();
            }
        }
        reader.setFilters((ArrayList<FilterVO>) request.get(Constants.BUNDLE_FILTERS));
        reader.isApplyFilter = true;
        if (newsItems == null || newsItems.isEmpty()) {
            getNewsItemList(request.hashCode(), request);
        }
        return NewsArticleVector.wrap(reader.applyFilters(newsItems), true);
    }

    public void showArticle(MBRequest mbRequest) {
        GoToArticleEvent event = new GoToArticleEvent();
        if (mbRequest.get(Constants.BUNDLE_ARTICLE_ID) == null) {
            event.setIdx(reader.getCurrentArticle());
        } else {
            if (mbRequest.get(Constants.BUNDLE_ARTICLE_ID) instanceof String) {
                String position = (String) mbRequest.get(Constants.BUNDLE_ARTICLE_ID);
                if (position.equals(Constants.ARTICLE_NEXT_POSITION)) {
                    reader.setCurrentArticle(reader.getCurrentArticle() + 1);
                    event.setIdx(reader.getCurrentArticle());
                } else if (position.equals(Constants.ARTICLE_PREVIOUS_POSITION)) {
                    reader.setCurrentArticle(reader.getCurrentArticle() - 1);
                    event.setIdx(reader.getCurrentArticle());
                }
            } else if (mbRequest.get(Constants.BUNDLE_ARTICLE_ID) instanceof Integer) {
                Integer position = (Integer) mbRequest.get(Constants.BUNDLE_ARTICLE_ID);
                event.setIdx(position);
            }
        }
        mb.send(NewsService.this, event);
    }

    public void expandArticle(MBRequest mbRequest) {
        ExpandArticleEvent event = new ExpandArticleEvent();
        Integer position = (Integer) mbRequest.get(Constants.BUNDLE_ARTICLE_ID);
        if (position != null) {
            event.setIdx(position);
        } else {
            event.setIdx(reader.getCurrentArticle());
        }
        mb.send(NewsService.this, event);
    }

    public Integer getArticle(MBRequest mbRequest) {
        return reader.getCurrentArticle();
    }

    public ModelDistribution getModelDistributions(MBRequest mbRequest) {
        return ReaderController.getModelDistributions();
    }


    // ****************************** HELPER CLASSES AND METHODS ***********************************

    private void checkTimer() {
        if (isTimerActivated == false) {
            timer.schedule(new NewsTimerTask(), mUpdateTime, mUpdateTime);
            isTimerActivated = true;
        }
    }

    private final class NewsAsyncFunction implements Callable<Void> {

        private MBRequest request;

        public NewsAsyncFunction(MBRequest request) {
            this.request = request;
        }

        public Void call() throws Exception {
            requests.put(request.hashCode(), request);
            reader.initialize(null, null, request.hashCode());
            return null;
        }
    }

    /**
     * This component checks whether there is an updated list of news articles or not.
     */
    private final class NewsTimerTask extends TimerTask {
        @Override
        public void run() {
            requestNews(MBRequest.build(Constants.MSG_UPDATE_NEWS)
                    .put(Constants.FLAG_UPDATE_NEWS, true));
        }
    }
}
