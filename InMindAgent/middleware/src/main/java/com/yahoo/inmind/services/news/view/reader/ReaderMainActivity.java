package com.yahoo.inmind.services.news.view.reader;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.middleware.R;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.control.util.MemUtil;
import com.yahoo.inmind.services.news.model.vo.NewsArticleVector;
import com.yahoo.inmind.services.news.view.browser.LoginBrowser;
import com.yahoo.inmind.services.news.view.handler.NewsHandler;
import com.yahoo.inmind.services.news.view.i13n.I13NActivity;

import java.util.LinkedList;
import java.util.Queue;


public class ReaderMainActivity extends I13NActivity{
    protected static final int MAX_MEMORY = 160;
    protected static final int MAX_CACHED_FRAGMENTS = 0;
    protected NewsListFragment mCurrentFrag = null;
    protected NewsHandler mUiHandler;
    protected Queue<NewsListFragment> que = new LinkedList<NewsListFragment>();//Cached pages
    protected int mIconResSwitchView = R.drawable.news_ic_flip;
    protected boolean bSwitchDisabled = true;
    protected FragmentManager fragmentManager;
    protected NewsListFragment currentFrag;
    private ReaderController reader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( savedInstanceState == null || (savedInstanceState != null
                && savedInstanceState.getInt( Constants.BUNDLE_MAIN_LAYOUT_ID ) == 0 ) ){
            setContentView( ReaderController.news_main);
        }else{
            setContentView( savedInstanceState.getInt( Constants.BUNDLE_MAIN_LAYOUT_ID ) );
        }

        // enable ActionBar app icon to behave as action to toggle nav drawer
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        reader = ReaderController.getInstance();
        ReaderController.setFlagRefreshAsyncListView(true);


        // Override all the UI components that you have been customized
        setLandscapeLayout((Integer) getIntent().getExtras().get(Constants.UI_LANDSCAPE_LAYOUT));
        setPortraitLayout((Integer) getIntent().getExtras().get(Constants.UI_PORTRAIT_LAYOUT));
        setUINewsRank((Integer) getIntent().getExtras().get(Constants.UI_NEWS_RANK));
        setUINewsTitle((Integer) getIntent().getExtras().get(Constants.UI_NEWS_TITLE));
        setUINewsSummary((Integer) getIntent().getExtras().get(Constants.UI_NEWS_SUMMARY));
        setUINewsImg((Integer) getIntent().getExtras().get(Constants.UI_NEWS_IMG));
        setUINewsPublisher((Integer) getIntent().getExtras().get(Constants.UI_NEWS_PUBLISHER));
        setUINewsReason((Integer) getIntent().getExtras().get(Constants.UI_NEWS_REASON));
        setUINewsScore((Integer) getIntent().getExtras().get(Constants.UI_NEWS_SCORE));
        setUINewsFeat((Integer) getIntent().getExtras().get(Constants.UI_NEWS_FEAT));
        setUINewsFeat2((Integer) getIntent().getExtras().get(Constants.UI_NEWS_FEAT2));
        setUINewsShareFB((Integer) getIntent().getExtras().get(Constants.UI_NEWS_SHARE_FB));
        setUINewsShareTwitter((Integer) getIntent().getExtras().get(Constants.UI_NEWS_SHARE_TWITTER));
        setUINewsShareTumblr((Integer) getIntent().getExtras().get(Constants.UI_NEWS_SHARE_TMBLR));
        setUINewsShareMore((Integer) getIntent().getExtras().get(Constants.UI_NEWS_SHARE_MORE));
        setUINewsLike((Integer) getIntent().getExtras().get(Constants.UI_NEWS_LIKE));
        setUINewsDislike((Integer) getIntent().getExtras().get(Constants.UI_NEWS_DISLIKE));
        setUINewsComments((Integer) getIntent().getExtras().get(Constants.UI_NEWS_COMMENTS));

        if( savedInstanceState != null
                && savedInstanceState.getBoolean( Constants.BUNDLE_RESET_SAVED_INSTANCE, false ) ){
            savedInstanceState = null;
        }

        reader.initialize(this, savedInstanceState, -1);
        //reader.messageId = -1;
        mUiHandler = reader.getUIHandler();
        NewsArticleVector.initialize( false );
    }

    public void setPortraitLayout( Integer portraitLayout ){
        reader.portrait_layout = portraitLayout;
    }

    public void setLandscapeLayout( Integer landscapeLayout ){
        reader.landscape_layout = landscapeLayout;
    }

    public void setUINewsRank( Integer uiNewsRank ){
        reader.news_rank = uiNewsRank;
    }

    public void setUINewsTitle( Integer uiNewsTitle ){
        reader.news_title = uiNewsTitle;
    }

    public void setUINewsScore( Integer uiNewsScore ){
        reader.news_score = uiNewsScore;
    }

    public void setUINewsSummary( Integer uiNewsSummary ){
        reader.news_summary = uiNewsSummary;
    }

    public void setUINewsFeat( Integer uiNewsFeat ){
        reader.news_feat = uiNewsFeat;
    }

    public void setUINewsFeat2( Integer uiNewsFeat2 ){
        reader.news_feat2 = uiNewsFeat2;
    }

    public void setUINewsPublisher( Integer uiNewsPublisher ){
        reader.news_publisher = uiNewsPublisher;
    }

    public void setUINewsReason( Integer uiNewsReason ){
        reader.news_reason = uiNewsReason;
    }

    public void setUINewsImg( Integer uiNewsImg ){
        reader.news_img = uiNewsImg;
    }

    public void setUINewsShareFB( Integer uiNewsShareFB ){
        reader.setNews_btnShareFb( uiNewsShareFB );
    }

    public void setUINewsShareTwitter( Integer uiNewsShareTwitter ){
        reader.setNews_btnShareTwitter( uiNewsShareTwitter );
    }

    public void setUINewsShareTumblr( Integer uiNewsShareTumblr ){
        reader.setNews_btnShareTumblr( uiNewsShareTumblr );
    }

    public void setUINewsShareMore( Integer uiNewsShareMore ){
        reader.setNews_btnShareMore( uiNewsShareMore );
    }

    public void setUINewsDislike( Integer uiNewsDislike ){
        reader.setNews_btnDislike( uiNewsDislike );
    }

    public void setUINewsComments( Integer uiNewsComments ){
        reader.news_comments = uiNewsComments;
    }

    public void setUINewsLike( Integer uiNewsLike ){
        reader.setNews_btnLike( uiNewsLike );
    }

    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_main, menu);
        return super.onCreateOptionsMenu(menu);
    }  
	
	@Override
	public void onBackPressed() {
//        Util.writeObjectToJsonFile( NewsArticleVector.getInstance(), Environment.DIRECTORY_DOWNLOADS, "news"); //NewsArticleVector.getVisitedList()
        reader.getSlingstone( null ).cancelTasks();
        reader.clear();
        super.onBackPressed();

        //moveTaskToBack(true);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        //super.recreate();
    }


    @Override
    protected void onPause(){
        super.onPause();

    }


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = reader.getDrawerManager(null).isDrawerOpen();
        menu.findItem(R.id.news_action_switchview).setIcon(getIconRes());
        menu.findItem(R.id.news_action_switchview).setVisible(!drawerOpen && reader.getSettings().isFlipViewEnabled());
        menu.findItem(R.id.news_action_switchview).setEnabled(!bSwitchDisabled);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
	protected void onResume() {
		super.onResume();
		invalidateOptionsMenu();
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (reader.getDrawerManager(null).onOptionsItemSelected(item)){
            return true;
        }
        int itemId = item.getItemId();
        if (itemId == R.id.news_action_switchview) {
			NewsListFragment frag = new NewsListFragment(getCurrentFrag().getItem());
			//Switch the layouts
			frag.setLayoutId(getCurrentFrag().getLayoutId() == ReaderController.news_fragment_flipview_layout_id
                    ? ReaderController.news_fragment_listview
                    : ReaderController.news_fragment_flipview_layout_id );
			frag.getItem().bDirty = false;
            Log.e("ReaderMainActivity.opt", "3. bklist before: " + frag.getItem().bklist.size() + "  list: " + frag.getItem().list.size());
			frag.getItem().bklist.addAll(frag.getItem().list);
			frag.getItem().list.clear();//to prevent listItem be freed when the old frag is removed -> destroyed
            Log.e("ReaderMainActivity.opt", "3. bklist after: " + frag.getItem().bklist.size() + "  list: " + frag.getItem().list.size());
            frag.getItem().frag = frag;
			frag.setLastItemIdx(getCurrentFrag().getLastItemIdx());
			frag.setScrollToPos(getCurrentFrag().getLastItemIdx());
			enableFragment(frag);
			mUiHandler.sendEmptyMessage(NewsHandler.SHOW_LOADING_COMPLETE);
			int layoutId = frag.getLayoutId();
			if (layoutId == ReaderController.news_fragment_flipview_layout_id ) {
				setIconRes(R.drawable.news_ic_list);
			} else if (layoutId == ReaderController.news_fragment_listview) {
				setIconRes(R.drawable.news_ic_flip);
			}
			bSwitchDisabled = true;
			enableSwitchViewDelayed();
			invalidateOptionsMenu();
			return true;
		} else if (itemId == R.id.news_action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
			return true;
		} else if( itemId == R.id.news_action_refresh ){
            Toast toast = Toast.makeText( this, "Next article...", Toast.LENGTH_SHORT );
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();

            // we need this otherwise doesn't process the last article of the screen
            while( NewsArticleVector.getEndPosBatch()
                    - (reader.getListView().getCurrentArticle()
                    + NewsArticleVector.getStartPosBatch() ) > 2 ) {
                NewsArticleVector.increaseCurrentPosition();
                reader.getListView().increaseCurrentArticle();
            }

            NewsArticleVector.processCurrentArticle(true, true);
            ReaderController.getListView().resetValues();
            return true;
        } else {
			return super.onOptionsItemSelected(item);
		}
    }
    
	public int getIconRes() {
		return mIconResSwitchView;
	}

	public void setIconRes(int iconResSwitchView) {
		this.mIconResSwitchView = iconResSwitchView;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {		
		super.onActivityResult(requestCode, resultCode, intent);
		switch(resultCode){
			case RESULT_OK:
                if( intent.getAction() != null ) {
                    if (intent.getAction().equals(LoginBrowser.LOGIN_SUCCESS)) {
                        if (getCurrentFrag().getItem() == null)//blank fragment
                        {
                            reader.getDrawerManager(null).selectItem(0, -1);//select the default item
                            break;
                        }
                        getCurrentFrag().getItem().bDirty = true;
                        reader.getDrawerManager(null).selectItem(mCurrentFrag.getItem().idx, -1);//reload data
                        reader.getDrawerManager(null).updateDrawerUserName();
                        reader.getDrawerManager(null).showDrawerSelectionAndClose(0);//set focus back to the first item after closing the drawer
                    }
                }
				break;
		}
	}
 	
	public void enableFragment(NewsListFragment frag) {//onCreateView() of the Fragment will then be called		
        fragmentManager = getFragmentManager();
        currentFrag = getCurrentFrag();
	
		if (currentFrag != null)//Cancel background loading for the Fragment losing focus
		{
			fragmentManager.beginTransaction().hide(currentFrag).commitAllowingStateLoss();
			fragmentManager.beginTransaction().detach(currentFrag).commitAllowingStateLoss();
			currentFrag.getItem().cancelLoadAsync();
			//Only enqueue when "empty" or "different"
			if (que.size() == 0 || !currentFrag.getItem().name.equals(frag.getItem().name))
			{
				if (que.contains(currentFrag))//remove all existing same fragments
					que.remove(currentFrag);
				que.add(currentFrag);
			}
		}
		//switch to the designated fragment
		if (!frag.isAdded())
			fragmentManager.beginTransaction().add( ReaderController.news_content_frame, frag).commitAllowingStateLoss();
		fragmentManager.beginTransaction().attach(frag).commitAllowingStateLoss();
		fragmentManager.beginTransaction().show(frag).commitAllowingStateLoss();
		
		//determine caching
		while ( (!que.isEmpty()) && (que.size() > MAX_CACHED_FRAGMENTS || MemUtil.getMemUsage() >= MAX_MEMORY) )
		{
			currentFrag = que.poll();
			currentFrag.partialFree();			
		}
        
		setCurrentFrag(frag);
		System.gc();
		
		enableSwitchViewDelayed();
	}

	private void enableSwitchViewDelayed() {
        reader.getUIHandler().postDelayed(new Runnable(){

			@Override
			public void run() {
				bSwitchDisabled = false;
				invalidateOptionsMenu();
			}
    		
    	}, 1500);
	}

	public NewsListFragment getCurrentFrag()
	{
		return mCurrentFrag;
	}
	
	public void setCurrentFrag(NewsListFragment frag) {
		if (frag == null)
			Log.e("inmind", "setCurrentFrag() set to null!");
		mCurrentFrag = frag;
		setIconRes(mCurrentFrag.getLayoutId() == ReaderController.news_fragment_flipview_layout_id
                ?R.drawable.news_ic_list
                :R.drawable.news_ic_flip);
	}
	
    @Override
    public void setTitle(CharSequence title) {
        reader.getDrawerManager(null).setTitle(title);
        getSupportActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        reader.getDrawerManager(null).syncState();       
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reader.getDrawerManager(null).onConfigurationChanged(newConfig);      
    }
   
	public DrawerManager getDrawerManager() {
		return reader.getDrawerManager(null);
	}
}