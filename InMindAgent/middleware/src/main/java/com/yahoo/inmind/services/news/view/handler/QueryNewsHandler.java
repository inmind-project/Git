package com.yahoo.inmind.services.news.view.handler;

import android.os.Bundle;
import android.os.Message;

import com.yahoo.inmind.services.news.control.reader.ReaderController;
import com.yahoo.inmind.services.news.model.source.ProfiledSource;
import com.yahoo.inmind.services.news.view.pluggableview.PluggableAdapterView;
import com.yahoo.inmind.services.news.view.reader.DrawerItem;

/**
 * Created by oscarr on 12/10/14.
 */
public class QueryNewsHandler extends NewsHandler {

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        DrawerItem item;
        switch(msg.what){
            case SHOW_LOADING:
                //Log.e("QueryNewsHandler.SHOW_LOADING", "5");
                //mAct.getCurrentFrag().setRefreshing(true);
                break;
            case SHOW_LOADING_COMPLETE:
                //Log.e("QueryNewsHandler.SHOW_LOADING_COMPLETE", "6");
                //mAct.getCurrentFrag().setRefreshing(false);
                break;
            case SHOW_FRAGMENT:
                //Log.e("QueryNewsHandler.SHOW_FRAGMENT", "7");
//                item = (DrawerItem) msg.obj;
//                if (item.frag != null)
//                    mAct.enableFragment(item.frag);
                break;
            case UPDATE_ASYNC_ITEMS:
                //Log.e("QueryNewsHandler.SHOW_LOADING", "8");
                msg.obj = null;
            case UPDATE_ASYNC_ITEM:
                //Log.e("QueryNewsHandler.UPDATE_ASYNC_ITEM", "9");
//                for (PluggableAdapterView listener : mItemReadyListeners)
//                {
//                    listener.onUpdate(msg.obj);
//                }
                break;
            case FOCUS_FRAGMENT:
                //Log.e("QueryNewsHandler.FOCUS_FRAGMENT", "10");
                //((NewsListFragment)msg.obj).onFocus();
                break;
            case REFRESH_DRAWER_ITEMS:
                //Log.e("QueryNewsHandler.REFRESH_DRAWER_ITEMS", "11");
                ProfiledSource src = (ProfiledSource) msg.obj;
                src.showExtendedOptions();
                break;
            case SCROLL_TO_ITEM:
                //Log.e("QueryNewsHandler.SCROLL_TO_ITEM", "12");
//				PluggableAdapterView pv = (PluggableAdapterView) msg.obj;
//				pv.scrollToIdx(msg.arg1);//the index
                break;
            case PREPARE_FOR_DOWNLOAD_DATA:
                //Log.e("QueryNewsHandler.PREPARE_FOR_DOWNLOAD_DATA", "13");
                item = (DrawerItem) msg.obj;
                if (item.frag != null)//refresh
                {
                    item.frag.clearAdapter();
                }
                Bundle data = msg.getData();
                int messageId = msg.arg1;
                msg = new Message();
                msg.obj = item;
                msg.what = DataHandler.INIT_ADAPTER_ON_DATA_THREAD;
                msg.arg1 = messageId;
                msg.setData( data );
                ReaderController.getInstance().getDataHandler().sendMessage(msg);
                break;
            case RE_ENABLE_SWITCH_VIEW:
                //Log.e("QueryNewsHandler.RE_ENABLE_SWITCH_VIEW", "14");
//                MenuItem menuItem = (MenuItem)msg.obj;
//                obj.setEnabled(true);
            default:
        }
    }




    @Override
    public void registerAsyncItemReadyListener(PluggableAdapterView l) {

    }

    @Override
    public void unregisterAsyncItemReadyListener(PluggableAdapterView l) {

    }
}
