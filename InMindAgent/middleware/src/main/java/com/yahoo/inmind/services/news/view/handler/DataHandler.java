package com.yahoo.inmind.services.news.view.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.yahoo.inmind.middleware.R;
import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.model.source.ProfiledSource;
import com.yahoo.inmind.services.news.view.reader.DrawerItem;
import com.yahoo.inmind.services.news.view.reader.NewsListFragment;

public class DataHandler extends Handler {

    public static final int INIT_ADAPTER_ON_DATA_THREAD = 0;
    public static final int BEGIN_LOAD_IN_BACKGROUND = 1;
    public static final int PROFILE_READY = 2;

    private NewsHandler mUiHandler = null;

    public DataHandler(Looper looper) {
        super(looper);
    }

    public void registerUiHandler(NewsHandler handler)
    {
        mUiHandler = handler;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case INIT_ADAPTER_ON_DATA_THREAD:
            {
//                Log.e("","INIT_ADAPTER_ON_DATA_THREAD");
                if( mUiHandler != null ) {
                    Message msgs = Message.obtain( null, NewsHandler.SHOW_FRAGMENT);
                    DrawerItem item = (DrawerItem) msg.obj;

                    if (item.frag == null)//Newly created
                    {
                        item.frag = new NewsListFragment(item);
                    }
                    msgs.obj = item;

                    if (!ReaderController.getInstance().isConnected())
                    {
                        Toast.makeText(ReaderController.get(), ReaderController.get().getString( R.string.news_not_connected ), Toast.LENGTH_SHORT).show();
                        break;
                    }

                    mUiHandler.sendMessage(msgs);
                    mUiHandler.sendEmptyMessage(NewsHandler.SHOW_LOADING);
                    item.loadSources( msg.arg1 );
                    mUiHandler.sendEmptyMessage(NewsHandler.SHOW_LOADING_COMPLETE);

                    Message msgf = new Message();
                    msgf.what = NewsHandler.FOCUS_FRAGMENT;
                    msgf.obj = item.frag;
                    mUiHandler.sendMessage(msgf);
                }else{
                    DrawerItem item = new DrawerItem();
                    item.loadAsync( null, 0);
                    item.loadSources( msg.arg1 );
                }
            }
            break;
            case BEGIN_LOAD_IN_BACKGROUND:
//                Log.e("","BEGIN_LOAD_IN_BACKGROUND");
                DrawerItem item = (DrawerItem) msg.obj;
                //Load images from here
                item.loadAsync(mUiHandler, NewsHandler.UPDATE_ASYNC_ITEM);
                mUiHandler.sendEmptyMessage(NewsHandler.UPDATE_ASYNC_ITEMS);//in case all drawables are loaded, but no one notifies.
                break;
            case PROFILE_READY:
//                Log.e("", "PROFILE_READY");
                ProfiledSource src = (ProfiledSource) msg.obj;
                src.generateItemsFromProfile();
                Message msgo = new Message();
                msgo.what = NewsHandler.REFRESH_DRAWER_ITEMS;
                msgo.obj = msg.obj;
                mUiHandler.sendMessage(msgo);
                break;
            default:
        }
    }
};
