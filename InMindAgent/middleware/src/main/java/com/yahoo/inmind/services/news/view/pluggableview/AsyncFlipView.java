package com.yahoo.inmind.services.news.view.pluggableview;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;

import com.aphidmobile.flip.FlipViewController;
import com.yahoo.inmind.services.news.model.i13n.Event;
import com.yahoo.inmind.services.news.control.i13n.I13N;
import com.yahoo.inmind.services.news.view.adapter.NewsAdapter;
import com.yahoo.inmind.services.news.model.vo.NewsArticle;
import com.yahoo.inmind.services.news.control.reader.ReaderController;

public class AsyncFlipView extends FlipViewController implements PluggableAdapterView<NewsArticle>{
	PositionChangedListener mPosChangedListener;
	ViewFlipListener mOnViewFlipListener;
	private static String pkgName = FlipViewController.class.getSimpleName();

	public AsyncFlipView(Context context) {
		super(context);
	}

	public AsyncFlipView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AsyncFlipView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AsyncFlipView(Context context, int flipOrientation) {
		super(context, flipOrientation);
	}
	
	@Override
	public void init(NewsAdapter adapter, Fragment frag) {
		setBackgroundColor(Color.BLACK);
		setAnimationBitmapFormat(Bitmap.Config.RGB_565);
		setAdapter(adapter);
		ReaderController.getInstance().getUIHandler().registerAsyncItemReadyListener(this);
	}
	
	@Override
	public void onUpdate(NewsArticle item) {
		if (getAdapter().isEmpty())
			return;
			
		if (item != null && item.getIdx() != null)
		{
			if (item.getIdx() == 0)
				((BaseAdapter) getAdapter()).notifyDataSetChanged();
			refreshPage(item.getIdx());
		}
		else//item is deliberately assigned to null, meaning refresh all.
		{
			((BaseAdapter) getAdapter()).notifyDataSetChanged();
			refreshAllPages();
		}
	}	
	
	@Override
	public void configureSwipeToRefresh(final View swipeLayout){
		mOnViewFlipListener = new ViewFlipListener(){
			@Override
			public void onViewFlipped(View view, int position) {
				I13N.get().log(new Event(pkgName, "Flip to item: " + (position + 1) )
					.setUuid(((NewsArticle) getAdapter().getItem(position)).getUuid()) );
				if (position == 0)
					swipeLayout.setEnabled(true);
				else
					swipeLayout.setEnabled(false);
				if (getPosChangedListener() != null)
				{
					getPosChangedListener().onPosChanged(position);
				}
			}
	    	
	    };
		setOnViewFlipListener(mOnViewFlipListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		ReaderController.getInstance().getUIHandler().registerAsyncItemReadyListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		ReaderController.getInstance().getUIHandler().unregisterAsyncItemReadyListener(this);
	}

	@Override
	public void onDestroyView() {
		ReaderController.getInstance().getUIHandler().unregisterAsyncItemReadyListener(this);
	}
	
	@Override
	public PositionChangedListener getPosChangedListener() {
		return mPosChangedListener;
	}
	
	@Override
	public void setPosChangedListener(PositionChangedListener mPosChangedListener) {
		this.mPosChangedListener = mPosChangedListener;
	}

	@Override
	public void scrollToIdx(int idx) {
		if (idx >= 0 && idx < getAdapter().getCount())
		{
			setSelection(idx);
			if (mOnViewFlipListener != null)
				mOnViewFlipListener.onViewFlipped(null, idx);
		}
	}
}
