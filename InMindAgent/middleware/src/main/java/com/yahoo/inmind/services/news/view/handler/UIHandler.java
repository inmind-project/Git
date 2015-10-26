package com.yahoo.inmind.services.news.view.handler;

import android.os.Bundle;
import android.os.Message;
import android.view.MenuItem;

import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.model.source.ProfiledSource;
import com.yahoo.inmind.services.news.view.pluggableview.PluggableAdapterView;
import com.yahoo.inmind.services.news.view.reader.DrawerItem;
import com.yahoo.inmind.services.news.view.reader.NewsListFragment;
import com.yahoo.inmind.services.news.view.reader.ReaderMainActivity;

import java.util.ArrayList;

public class UIHandler extends NewsHandler
{
    ReaderMainActivity mAct = null;
    ArrayList<PluggableAdapterView> mItemReadyListeners = new ArrayList<PluggableAdapterView>();

    public UIHandler(ReaderMainActivity readerActivity) {
        mAct = readerActivity;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        DrawerItem item;
        switch(msg.what){
            case SHOW_LOADING:
//                Log.e("", "SHOW_LOADING");
                mAct.getCurrentFrag().setRefreshing(true);
                break;
            case SHOW_LOADING_COMPLETE:
//                Log.e("", "SHOW_LOADING_COMPLETE");
                mAct.getCurrentFrag().setRefreshing(false);
                break;
            case SHOW_FRAGMENT:
//                Log.e("", "SHOW_FRAGMENT");
                item = (DrawerItem) msg.obj;
                if (item.frag != null)
                    mAct.enableFragment(item.frag);
                break;
            case UPDATE_ASYNC_ITEMS:
//                Log.e("", "UPDATE_ASYNC_ITEMS");
                msg.obj = null;
            case UPDATE_ASYNC_ITEM:
//                Log.e("", "UPDATE_ASYNC_ITEM");
                for (PluggableAdapterView listener : mItemReadyListeners){
                    listener.onUpdate(msg.obj);
                }
                break;
            case FOCUS_FRAGMENT:
//                Log.e("", "FOCUS_FRAGMENT");
                ((NewsListFragment)msg.obj).onFocus();
                break;
            case REFRESH_DRAWER_ITEMS:
//                Log.e("", "REFRESH_DRAWER_ITEMS");
                ProfiledSource src = (ProfiledSource) msg.obj;
                src.showExtendedOptions();
                break;
            case SCROLL_TO_ITEM:
//                Log.e("", "SCROLL_TO_ITEM");
//				PluggableAdapterView pv = (PluggableAdapterView) msg.obj;
//				pv.scrollToIdx(msg.arg1);//the index
                break;
            case PREPARE_FOR_DOWNLOAD_DATA:
//                Log.e("", "PREPARE_FOR_DOWNLOAD_DATA");
                item = (DrawerItem) msg.obj;
                if (item.frag != null && ReaderController.isFlagRefreshAsyncListView() )//refresh
                {
                    item.frag.clearAdapter();
                }
                if( item.frag == null || ( item.frag != null && ReaderController.isFlagRefreshAsyncListView() ) ) {
                    Bundle data = msg.getData();
                    int messageId = msg.arg1;
                    msg = new Message();
                    msg.arg1 = messageId;
                    msg.obj = item;
                    msg.what = DataHandler.INIT_ADAPTER_ON_DATA_THREAD;
                    msg.setData(data);
                    ReaderController.getInstance().getDataHandler().sendMessage(msg);
                } else if( item.frag != null ){
                    item.frag.onFocus();
                }
                break;
            case RE_ENABLE_SWITCH_VIEW:
//                Log.e("", "RE_ENABLE_SWITCH_VIEW");
                MenuItem menuItem = (MenuItem)msg.obj;
                menuItem.setEnabled(true);
            default:
        }
    }

    /**
     * Register to be notified when the AsyncSource, if any, finishes loading every item.
     * The listener's onUpdate() will be called given the item given in AsyncSource::loadItemInParallel()
     * on a UI thread, so UI refresh could be done in the AsyncItemReadyListener's onUpdate().
     *
     * */
    public void registerAsyncItemReadyListener(PluggableAdapterView l)
    {
        if (!mItemReadyListeners.contains(l))
            mItemReadyListeners.add(l);
    }

    public void unregisterAsyncItemReadyListener(PluggableAdapterView l)
    {
        mItemReadyListeners.remove(l);
    }
}
