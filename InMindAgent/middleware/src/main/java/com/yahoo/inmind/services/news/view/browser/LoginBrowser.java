package com.yahoo.inmind.services.news.view.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.yahoo.inmind.services.news.control.i13n.I13N;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.control.util.DevUtil;
import com.yahoo.inmind.services.news.model.i13n.Event;

public class LoginBrowser extends BaseBrowser {
	private static final String server = "blondbeyond.corp.ne1.yahoo.com";
//	private static final String server = "192.168.1.128";
	final public static String scheme = "http://";
	final public static String port = ":4080";
	final public static String baseUrl = scheme + server + port + "/";
	//final public static String loginUrl = "https://by.bouncer.login.yahoo.com/login/?force_login=1";
	public final static String attrs = "?devid=" + DevUtil.getDeviceID() + "&type=general";
	public static String loginUrl = baseUrl + "login" + attrs;
	 
	//final public static String successfulUrl = "https://by.bouncer.login.yahoo.com/admin/";
	final public static String successfulUrl = baseUrl + "loginsuccess";
	final public static String LOGIN_SUCCESS = "GG";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		String configUrl = ReaderController.getInstance().getConfig().getString("LOGIN_URL");
		if (configUrl != null)
			loginUrl = configUrl + attrs;
	}

	@Override
	protected void initExtraFunctions(WebView wv) {	
		super.initExtraFunctions(wv);
		wv.getSettings().setJavaScriptEnabled(false);
	}

	@SuppressLint("NewApi")
	@Override
	protected void initWevViewClient(WebView wv) {
		wv.getSettings().setLoadsImagesAutomatically(false);
		wv.setWebViewClient(new WebViewClient()       
        {
			@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) 
            {	
				view.loadUrl(url);
				return true;
            }
			

			@Override
			public void onPageFinished(WebView view, String url)
			{
				if (url.equals(successfulUrl))
           	 	{
	           		ReaderController.getInstance().getCookieStore().saveLoginCookies(server);
	           		Toast.makeText(ReaderController.get(), "Login Successfully", Toast.LENGTH_LONG).show();
	           		setResult(RESULT_OK, new Intent(LOGIN_SUCCESS));	
	           		I13N.get().log(new Event(this.getClass().getSimpleName(), "Login Success"));
	           		finish();
           	 	}
			}
			
        });
	}
	
}
