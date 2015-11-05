package com.yahoo.inmind.services.news.view.slingstone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.middleware.R;
import com.yahoo.inmind.services.news.control.cache.ImgLruCacher;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.control.share.ShareHelper;
import com.yahoo.inmind.services.news.control.util.WebImgUtil;
import com.yahoo.inmind.services.news.model.events.NewsUIRenderEvent;
import com.yahoo.inmind.services.news.model.vo.JsonItem;
import com.yahoo.inmind.services.news.model.vo.NewsArticle;
import com.yahoo.inmind.services.news.view.browser.BaseBrowser;

import java.lang.ref.WeakReference;

import com.yahoo.inmind.comm.generic.control.eventbus.EventBus;

public class SlingstoneRenderer extends Renderer {
    private WebImgUtil mImgUtil;
    private ImgLruCacher mCache;
    private ReaderController reader;
    private EventBus bus;
    private Context context;


    public SlingstoneRenderer(Context ctx) {
        super(ctx);
        reader = ReaderController.getInstance();
        mResId = reader.getLandscapeLayout();
        mImgUtil = new WebImgUtil(ctx);
        bus = EventBus.getDefault();
        context = ctx;
    }

    @Override
    public boolean isCompatible(JsonItem item) {
        if (item instanceof JsonItem)
            return true;
        return false;
    }

    @Override
    public boolean isDirty(View v, JsonItem jsonItem) {

        if (jsonItem == null || v == null || !(jsonItem instanceof NewsArticle))
            return true;
        WeakReference<NewsArticle> weakRef = (WeakReference<NewsArticle>)v.getTag();
        if (weakRef == null || weakRef.get() == null)
            return true;
        NewsArticle newItem = (NewsArticle) jsonItem;
        NewsArticle oldItem = (NewsArticle) weakRef.get();
        Drawable d1, d2;

        if (mCache == null)
        {
            d1 = reader.getDrawableNewsItem( oldItem.getIdx() );
            d2 = reader.getDrawableNewsItem( newItem.getIdx() );
            if (d2 == null && d1 == null)
                return false;
            else if (d2 != null && d1 != null)
            {
                if (d1.getIntrinsicHeight() > d1.getIntrinsicWidth() && d2.getIntrinsicHeight() > d2.getIntrinsicWidth()){
                    return false;
                }
                if (d1.getIntrinsicHeight() < d1.getIntrinsicWidth() && d2.getIntrinsicHeight() < d2.getIntrinsicWidth()){
                    return false;
                }
            }
        }
        else
        {
            return oldItem.getDimension() != newItem.getDimension();
        }

        return true;
    }

    //Draw the view of each list item
    @Override
    public View inflate(View view, JsonItem item, ViewGroup vg) {
        if (view != null){ //view exists but is dirty
            freeView(view);
        }

        NewsArticle  art = (NewsArticle) item;
        if (mCache == null){//No cache is used
            Drawable d = reader.getDrawableNewsItem( art.getIdx() );
            if (d != null){
                if (d.getIntrinsicWidth() < d.getIntrinsicHeight()){
                    view = mInflater.inflate(reader.getPortraitLayout(), null);
                }
            }
        }
        else{ //Use Cache
            switch(art.getDimension()){
                case NewsArticle.DIM_PORTRAIT:
                    view = mInflater.inflate(reader.getPortraitLayout(), null);
                    break;
                case NewsArticle.DIM_LANDSCAPE:
                    view = mInflater.inflate(reader.getLandscapeLayout(), null);
                    break;
                case NewsArticle.DIM_UNDEFINED:
                default:
            }
        }

        //Default view when dimension of the image is not ready
        if (view == null) {
            view = mInflater.inflate(reader.getLandscapeLayout(), null);
        }
        return view;
    }

    //Fill every field in the View with data
    @Override
    public void render(View view, JsonItem item, int idx) {
        NewsUIRenderEvent event = new NewsUIRenderEvent();
        NewsArticle art = (NewsArticle) item;

        if (art.getSummary() != null) {
            TextView tv = event.setUiTVSummary(reader.getUiTVSummary(view));
            tv.setText(art.getSummary());
            addClickListener(art, tv);
        }
        if (art.getPublisher() != null)
            event.setUiTVPublisher(reader.getUiTVPublisher(view)).setText(art.getPublisher());
        if (art.getReason() != null)
            event.setUiTVReason(reader.getUiTVReason(view)).setText(art.getReason());


        //Add click listeners
        ImageView inner_iv = event.setUiIVImg(reader.getUiIVImg(view));
        addClickListener(art, inner_iv);
        addClickListener(art, view);//Add clickListener to the container

        //Set onclick listener for sharing buttons
//        event.setUiIBDislike(reader.getUiIBDislike(view)),
//                event.setUiIBLike(reader.getUiIBLike(view))
        for (ImageButton btn : new ImageButton[]{event.setUiIBShareFb(reader.getUiIBShareFb(view)),
                event.setUiIBShareTwitter(reader.getUiIBShareTwitter(view)),
                event.setUiIBShareTumblr(reader.getUiIBShareTumblr(view)),
                event.setUiIBShareMore(reader.getUiIBShareMore(view)),
                }) {
            btn.setTag(new WeakReference<>(art));
            btn.setOnClickListener(onShareClickListener);
        }

        if (art.getTitle() != null)
            event.setUiTVTitle(reader.getUiTVTitle(view)).setText(art.getTitle());

        if (reader.showCategories) {
            event.setUiTVRank(reader.getUiTVRank(view)).setText(String.valueOf(idx + 1));
            if (art.getScore() != null)
                event.setUiTVScore(reader.getUiTVScore(view)).setText("Score: "
                        + (art.getScore().length() >= 5 ? art.getScore().substring(0, 5)
                        : art.getScore()));
            if (art.getRawScores() != null) {
                event.setUiTVFeat(reader.getUiTVFeat(view)).setText("Raw Scores:\n"
                        + Util.listToString(art.getRawScores()));
            }
            if (art.getCapFeatures() != null) {
                TextView tv = event.setUiTVFeat2(reader.getUiTVFeat2(view));
                tv.setText("CAP Features:\n" + Util.listToString(art.getCapFeatures()));
                addClickListener(art, tv);
            }
            TextView tv1 = (TextView) view.findViewById( ReaderController.news_recommendation1 );
            tv1.setText("Recommended by Emma's algorithm: " + (art.isRecommendation1() ? "YES" : "NO"));
            TextView tv2 = (TextView) view.findViewById(ReaderController.news_recommendation2);
            tv2.setText("Recommended by William's algorithm: " + (art.isRecommendation2() ? "YES" : "NO"));
            // TextView for user comments
            art.setTvComments(reader.getUiTvComments(view));
            createCommentsEditText(art);
        } else {
            reader.getUiTVRank(view).setVisibility(View.INVISIBLE);
            reader.getUiTVRank(view).setHeight(0);
            reader.getUiTVScore(view).setVisibility(View.INVISIBLE);
            reader.getUiTVScore(view).setHeight(0);
            reader.getUiTVFeat(view).setVisibility(View.INVISIBLE);
            reader.getUiTVFeat(view).setHeight(0);
            reader.getUiTVFeat2(view).setVisibility(View.INVISIBLE);
            reader.getUiTVFeat2(view).setHeight(0);
            TextView tv1 = (TextView) view.findViewById(ReaderController.news_recommendation1);
            TextView tv2 = (TextView) view.findViewById(ReaderController.news_recommendation2);
            if (tv1 != null && tv2 != null) {
                tv1.setVisibility(View.INVISIBLE);
                tv1.setHeight(0);
                tv2.setVisibility(View.INVISIBLE);
                tv2.setHeight(0);
            }
        }


        // notify observers...
        if (bus.hasSubscriberForEvent(NewsUIRenderEvent.class)) {
            bus.post(event);
        }

        //Set ImageView
        //Case 1: Use in-memory drawable without cache
        if (reader.getDrawableNewsItem(art.getIdx()) != null) {
            setImageView(event.setUiIVImg(reader.getUiIVImg(view)),
                    reader.getDrawableNewsItem(art.getIdx()));
            if (bus.hasSubscriberForEvent(NewsUIRenderEvent.class)) {
                bus.post(event);
            }
            return;
        }

        //Case 2: Use cache
        if (mCache != null)//use cache to retrieve bitmap & drawable
        {
            if (art.getImgPath() != null)//downloaded, so the Drawable deserved not to be null
            {
                Bitmap cachedBmp = mCache.get(art.getImgPath());
                setImageView(event.setUiIVImg(reader.getUiIVImg(view)),
                        mImgUtil.drawableFromBmp(cachedBmp));
                if (bus.hasSubscriberForEvent(NewsUIRenderEvent.class)) {
                    bus.post(event);
                }
                return;
            }
        }

        //Case 3: The image is not ready, neither in the persistent storage nor in memory.
        ImageView iv = event.setUiIVImg(reader.getUiIVImg(view));
        iv.setImageDrawable(null);
        iv.setVisibility(View.GONE);
    }

    private void addClickListener(NewsArticle art, View container) {
        container.setTag(new WeakReference<>(art));
        container.setOnClickListener(mOnItemClickListener);
    }

    OnClickListener mOnItemClickListener = new OnClickListener(){

        @Override
        public void onClick(View v) {
            NewsArticle article = (((WeakReference<NewsArticle>) v.getTag()).get());
            String url = article.getUrl();
            String uuid = article.getUuid();
            article.setClickOnNews( true );
            ReaderController.flagClickOnArticle = true;
            Intent intent = new Intent(getContext(), BaseBrowser.class);
            intent.putExtra("url", url);
            intent.putExtra("uuid", uuid);
            getContext().startActivity(intent);
        }

    };

    @Override
    public void freeView(View view) {//This is very important.
        NewsUIRenderEvent event = new NewsUIRenderEvent();
        ImageView iv = event.setUiIVImg(reader.getUiIVImg(view));
        iv.setImageDrawable(null);
        if( bus.hasSubscriberForEvent( NewsUIRenderEvent.class) ) {
            bus.post(event);
        }
    }

    public void setImageView(View view, Drawable d) {
        NewsUIRenderEvent event = new NewsUIRenderEvent();
        ImageView iv = event.setUiIVImg( reader.getUiIVImg(view));
        iv.setImageDrawable(d);
        iv.setScaleType(ScaleType.FIT_CENTER);
        iv.setMinimumHeight(400);
        iv.setVisibility(View.VISIBLE);
        if( bus.hasSubscriberForEvent( NewsUIRenderEvent.class) ) {
            bus.post(event);
        }
    }

    public Renderer enableCache(ImgLruCacher cache) {
        mCache = cache;
        return this;
    }

    //The OnClickListeners for sharing buttons
    private OnClickListener onShareClickListener = new OnClickListener(){

        @Override
        public void onClick(View v) {
            WeakReference<NewsArticle> itemRef = (WeakReference<NewsArticle>) v.getTag();
            final NewsArticle item = itemRef.get();
            if (item == null)
                return;
            ShareHelper.Type type = ShareHelper.Type.More;
            int id = v.getId();
            if (id == reader.getNews_btnShareFb() ) {
                type = ShareHelper.Type.Facebook;
            } else if (id == reader.getNews_btnShareTwitter()) {
                type = ShareHelper.Type.Twitter;
            } else if (id == reader.getNews_btnShareTumblr()) {
                type = ShareHelper.Type.Tumblr;
            } else if (id == reader.getNews_btnShareMore()) {
                type = ShareHelper.Type.More;
            } else if (id == reader.getNews_btnDislike()) {
                type = ShareHelper.Type.Dislike;
            } else if (id == reader.getNews_btnLike()) {
                type = ShareHelper.Type.Like;
            }
            ReaderController.getInstance().getShareHelper().share(type, getContext(),
                    item.getTitle(), item.getSummary(), item.getUrl(), item.getUuid(), item.getIdx());

        }

    };



    private void createCommentsEditText( final NewsArticle article ){
        final EditText comments = ((EditText) article.getTvComments());
        comments.setSelected(true);
        comments.setVisibility(View.VISIBLE);

        if( article.getUserComments() == null || article.getUserComments().equals("") ) {
            comments.setText(context.getString(R.string.news_write_comments));
        }else{
            comments.setText( article.getUserComments() );
        }

//        comments.addTextChangedListener( new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Log.e("","afterTextChanged  " + s);
//                article.setUserComments( comments.getText().toString() );
//            }
//        });
        comments.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (comments.getText().toString().startsWith( context.getString( R.string.news_write_comments )) ) {
                    comments.setText("");
                }else if( !hasFocus ) {
                    article.setUserComments(comments.getText().toString());
                }
            }
        });
        comments.setOnEditorActionListener( new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ) {
                    comments.clearFocus();
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });
    }
}
