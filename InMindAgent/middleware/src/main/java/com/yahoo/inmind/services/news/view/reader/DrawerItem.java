package com.yahoo.inmind.services.news.view.reader;

import android.content.Intent;

import com.yahoo.inmind.services.news.model.source.AsyncSource;
import com.yahoo.inmind.services.news.model.source.Source;
import com.yahoo.inmind.services.news.model.vo.JsonItem;
import com.yahoo.inmind.services.news.view.handler.NewsHandler;
import com.yahoo.inmind.services.news.view.slingstone.Renderer;

import java.util.ArrayList;


public class DrawerItem {
    public String name = null;
    public ArrayList<Source> srcs = new ArrayList<Source>();
    public ArrayList<Renderer> renderers = new ArrayList<Renderer>();
    public ArrayList<JsonItem> list = new ArrayList<JsonItem>();
    public static ArrayList<JsonItem> bklist = new ArrayList<JsonItem>();
    public NewsListFragment frag = null;
    public Intent intent = null;
    public int idx = -1;
    public DrawerManager parent;
    public boolean bDirty = true;


    public DrawerItem(){
    }

    //must be executed in background thread
    public void loadSources( int messageId )
    {
        for (Source src : srcs) {
            src.fetchData(bklist, messageId );
        }
    }

    public DrawerManager getParent()
    {
        return parent;
    }

    public void loadAsync(NewsHandler UIhandler, int msgWhatAfterCompletion){
        cancelLoadAsync();
        for (Source src : srcs)
        {
            if (src instanceof AsyncSource)
            {
                ((AsyncSource) src).loadAsync(UIhandler, list, msgWhatAfterCompletion);
            }
        }
    }

    public void cancelLoadAsync(){
        for (Source src : srcs)
        {
            if (src instanceof AsyncSource)
            {
                ((AsyncSource) src).cancelLoadAsync();
            }
        }
    }
}