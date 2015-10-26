package com.yahoo.inmind.services.news.view.adapter;

import java.util.ArrayList;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yahoo.inmind.services.news.model.vo.JsonItem;
import com.yahoo.inmind.services.news.model.vo.NewsArticle;
import com.yahoo.inmind.services.news.view.reader.DrawerItem;
import com.yahoo.inmind.services.news.view.slingstone.Renderer;


public class NewsAdapter extends BaseAdapter {
	private ArrayList<JsonItem> mList = null;
	private ArrayList<Renderer> mRenderers = null;
    private DrawerItem mItem = null;


    public void setDrawerItem( DrawerItem item ){
        mItem = item;
    }

	public void setList(ArrayList<JsonItem> list)
	{
		if( mList != null ) {
            mList.clear();
        }
        mList = list;
        if( mItem != null && mItem.list != mList) {
            mItem.list = mList;
        }
        if( mList == null ){
            Log.e("NewsAdapter", "mList null");
        }
    }

	public ArrayList<JsonItem> getmList() {
		return mList;
	}

	public void setRenderer(ArrayList<Renderer> rens)
	{
		mRenderers = rens;
	}
	
	@Override
	public int getCount() {
		if (mList == null)
			return 0;
		return mList.size();
	}

	@Override
	public JsonItem getItem(int i) {
		if (mList == null)
			return null;
		return mList.get(i);
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public int getItemViewType(int arg0) {
		if (mList == null || mList.size() == 0)
			return 3;
//        Log.e("NewsAdapter", "arg: "+arg0);
//        Log.e("NewsAdapter", "list newsSize: "+mList.newsSize());
//        Log.e("NewsAdapter", "element: "+mList.get(arg0));
        if( mList.get(arg0) == null || ((NewsArticle) mList.get(arg0)).getDimension() == null){
            return -1;
        }else {
//            Log.e("NewsAdapter", "value " + ((NewsArticle) mList.get(arg0)).dimension);
            return ((NewsArticle) mList.get(arg0)).getDimension();
        }
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (i >= mList.size())
			return null;
		JsonItem item = mList.get(i);
        Renderer ren = getCompatibleRenderer(item);
        if (view == null || ren.isDirty(view, item)) {
            view = ren.inflate(view, item, viewGroup);
        } else {
            ren.freeView(view);
        }
        ren.render(view, item, i);
        return view;
	}

	private Renderer getCompatibleRenderer(JsonItem item) {
		for (Renderer ren : mRenderers)
		{
			if (ren.isCompatible(item))
				return ren;
		}
		return null;
	}	

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public boolean isEmpty() {
		if (mList == null)
			return true;
		// TODO Auto-generated method stub
		return mList.isEmpty();
	}

	public void partialFree(){
		for (JsonItem item : mList)
			item.partialFree();
	}
	
	public void clear() {
        if( mList != null ) {
            for (JsonItem item : mList)
                item.free();
            mList.clear();
        }
	}
}
