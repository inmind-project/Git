package com.yahoo.inmind.services.news.view.pluggableview;

import android.app.Fragment;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.yahoo.inmind.services.news.model.events.GoToArticleEvent;
import com.yahoo.inmind.services.news.model.events.RefreshNewsListEvent;
import com.yahoo.inmind.services.news.model.i13n.Event;
import com.yahoo.inmind.services.news.view.i13n.I13NListView;
import com.yahoo.inmind.services.news.view.adapter.NewsAdapter;
import com.yahoo.inmind.services.news.model.vo.NewsArticle;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.control.util.MemUtil;

import de.greenrobot.event.EventBus;

public class AsyncListView extends I13NListView implements PluggableAdapterView<NewsArticle> {
    PositionChangedListener mPosChangedListener;
    private int previousArticle;
    private int currentArticle;
    private double dwellTime;
    private EventBus bus;
    private int offset;

    public AsyncListView(Context context) {
        super(context);
        initialize();
    }

    public AsyncListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public AsyncListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }


    private void initialize(){
        instrument();
        bus = EventBus.getDefault();
        bus.register(this);
        ReaderController.setListView(this);
        resetValues();
    }

    public void resetValues(){
        previousArticle = 0;
        currentArticle = 0;
        offset = NewsArticleVector.getCurrentPos();
        dwellTime = 0;
    }

    /***
     * @author  oscarr
     */
    public void onEventMainThread( final GoToArticleEvent event ){
        this.scrollToIdx(event.getIdx());
        ReaderController.getInstance().setCurrentArticle(event.getIdx());
    }

    public void onEventMainThread( final RefreshNewsListEvent event ){
        NewsAdapter adapter = ((NewsAdapter) getAdapter());
        adapter.setList(NewsArticleVector.getJsonItemlist(event.getArticleList()));
        setAdapter(adapter);
        ((NewsAdapter) getAdapter()).notifyDataSetChanged();
        invalidateViews();
        ReaderController.setFlagRefreshAsyncListView(false);
    }


    @Override
    protected void addItemData(int position, Event evt) {
        if (getAdapter().isEmpty())
            return;
        Object obj = getAdapter().getItem(position);
        if (!(obj instanceof NewsArticle))
            return;
        NewsArticle item = (NewsArticle) obj;
        evt.uuid = item.getUuid();
        if (getPosChangedListener() != null)
            getPosChangedListener().onPosChanged(position);
    }

    @Override
    public void init(NewsAdapter adapter, Fragment frag) {
        setAdapter(adapter);
        setParent(frag);
        MemUtil.disableListViewCache(this);
        ReaderController.getInstance().getUIHandler().registerAsyncItemReadyListener(this);
    }

    @Override
    public void onUpdate(NewsArticle item) {
        //Log.e("DEBUG", "updating article: " + (item == null ? "null" :  item.getTitle().substring(0, 10) + "  url: " + item.getImgPath() ) );
        ((BaseAdapter) getAdapter()).notifyDataSetChanged();
        if (!isFocusable()) {
            invalidateViews();
        }
    }

    @Override
    public void configureSwipeToRefresh(final View swipeLayout) {
        setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (currentArticle != firstVisibleItem) {
                    previousArticle = currentArticle;
                    currentArticle = firstVisibleItem;
                    if (previousArticle != currentArticle && NewsArticleVector.getNumArtPerView() > 1) {
                        NewsArticleVector.processCurrentArticle(previousArticle < currentArticle, false);
                    }
                }
//                Log.e("","firstVisibleItem: " +firstVisibleItem + " visibleItemCount: " +visibleItemCount
//                        + " totalItemCount: "+totalItemCount+ " previousArticle: " + previousArticle
//                        + " currentArticle: " + currentArticle);


                if (firstVisibleItem == 0) {
                    swipeLayout.setEnabled(true);
                } else {
                    swipeLayout.setEnabled(false);
                }
            }
        });
    }

    public void setDwellTime( int idx ){
        if( NewsArticleVector.getInstance().size() > idx && idx >= 0 ) {
            double dwellTemp1 = (System.currentTimeMillis() - dwellTime) / 1000d; //seconds
            NewsArticle article = NewsArticleVector.getInstance().get(idx);
            double dwellTemp2 = article.getDwellTime();
            article.setDwellTime(dwellTemp1 + dwellTemp2);
            offset = NewsArticleVector.getCurrentPos();
        }
    }

    @Override
    public void onResume() {
        ReaderController.getInstance().getUIHandler().registerAsyncItemReadyListener(this);
        if( ReaderController.flagClickOnArticle == false ) {
            dwellTime = System.currentTimeMillis();
        }else{
            ReaderController.flagClickOnArticle = false;
        }
    }

    @Override
    public void onPause() {
        ReaderController.getInstance().getUIHandler().unregisterAsyncItemReadyListener(this);
    }

    @Override
    public void onDestroyView() {
        ReaderController.getInstance().getUIHandler().unregisterAsyncItemReadyListener(this);
    }


    @Override
    public PositionChangedListener getPosChangedListener() {
        return mPosChangedListener;
    }

    @Override
    public void setPosChangedListener(PositionChangedListener mPosChangedListener) {
        this.mPosChangedListener = mPosChangedListener;
    }

    @Override
    public void scrollToIdx(int idx) {
        if (idx >= 0 && idx < getAdapter().getCount())
            setSelection(idx);
        clearAnimation();
    }

    public int getCurrentArticle() {
        return currentArticle;
    }

    public void increaseCurrentArticle() {
        currentArticle++;
    }
}
