package com.yahoo.inmind.services.news.view.handler;

import android.os.Handler;

import com.yahoo.inmind.services.news.view.pluggableview.PluggableAdapterView;


/**
 * Created by oscarr on 12/10/14.
 */
public abstract class NewsHandler extends Handler {

    public static final int SHOW_LOADING = 100;
    public static final int SHOW_LOADING_COMPLETE = 101;
    public static final int UPDATE_ASYNC_ITEM = 102;
    public static final int SHOW_FRAGMENT = 103;
    public static final int FOCUS_FRAGMENT = 104;
    public static final int REFRESH_DRAWER_ITEMS = 105;
    public static final int UPDATE_ASYNC_ITEMS = 106;
    public static final int SCROLL_TO_ITEM = 107;
    public static final int PREPARE_FOR_DOWNLOAD_DATA = 108;
    public static final int RE_ENABLE_SWITCH_VIEW = 109;

    public abstract void registerAsyncItemReadyListener(PluggableAdapterView l);

    public abstract void unregisterAsyncItemReadyListener(PluggableAdapterView l);
}
