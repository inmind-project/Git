package com.yahoo.inmind.services.news.view.browser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.yahoo.inmind.services.news.control.i13n.I13N;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.model.i13n.Event;
import com.yahoo.inmind.services.news.view.reader.BackableActivity;

import java.lang.reflect.Field;


public class BaseBrowser extends BackableActivity {
	WebView mWv;
	private WebViewClient mWebviewClient;
	private WebChromeClient mWebChromeClient;
	public static final String LAUCH_BROWSER_URL = "url";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView( ReaderController.news_browser_layout_id );
		WebView wv = (WebView) findViewById(ReaderController.news_wv);
		mWv = wv;
        initExtraFunctions(wv);
		
		Intent intent = getIntent();
		loadUrl(intent.getStringExtra(LAUCH_BROWSER_URL));
		
		Event evt = getEvent();
		evt.uuid = intent.getStringExtra("uuid");
		setEvent(evt);

		setConfigCallback((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
	}
	
	private void loadUrl(String url){
        mWv.loadUrl(url);
		System.gc();
		I13N.get().log(new Event(getEvent()).setAction("load url: " + url));
	}
	
	protected void initExtraFunctions(WebView wv) {
    	initWevViewClient(wv);//Prevent WebView from opening a browser
		initProgressBar(wv);//Show progress bar
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(true);
    }
	
	protected void initWevViewClient(WebView wv) {
    	mWebviewClient = new WebViewClient()
        {	
			 @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) 
            {	 
				 loadUrl(url);
            	 return true;
            }
        };
		wv.setWebViewClient(mWebviewClient);
    }

	protected void initProgressBar(WebView wv) {
    	final ProgressBar Pbar;
		Pbar = (ProgressBar) findViewById( ReaderController.news_pB1 );
		mWebChromeClient = new WebChromeClient() {
			
			public void onProgressChanged(WebView view, int progress) 
	        {
    			if(progress < 100 && Pbar.getVisibility() == ProgressBar.GONE)
				{
					Pbar.setVisibility(ProgressBar.VISIBLE);                    
				}
		        Pbar.setProgress(progress);
		        if(progress == 100) 
		        {
		            Pbar.setVisibility(ProgressBar.GONE);                   
		        }
	           }
			
		};
		wv.setWebChromeClient(mWebChromeClient);
    }
	
	public void setConfigCallback(WindowManager windowManager) {
	    try {
            Field field = WebView.class.getDeclaredField("mWebViewCore");
            field = field.getType().getDeclaredField("mBrowserFrame");
            field = field.getType().getDeclaredField("sConfigCallback");
            field.setAccessible(true);
            Object configCallback = field.get(null);
            if (null == configCallback) {
	            return;
	        }
            field = field.getType().getDeclaredField("mWindowManager");
	        field.setAccessible(true);
	        field.set(configCallback, windowManager);
        } catch(Exception e) {
	    }
	}


	@Override
	public void onBackPressed(){
		if (mWebviewClient != null)
			mWebviewClient = null;
		if (mWebChromeClient != null)
			mWebChromeClient = null;

		if (mWv == null)
			return;
		mWv.stopLoading();
		mWv.clearCache(true);
		mWv.clearView();
		mWv.freeMemory();

		ViewGroup vg = (ViewGroup)(mWv.getParent());
		vg.removeView(mWv);
		mWv.destroy();
		System.gc();
		mWv = null;
		setConfigCallback(null);
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
