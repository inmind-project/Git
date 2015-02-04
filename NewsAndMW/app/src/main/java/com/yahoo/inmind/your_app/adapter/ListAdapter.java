package com.yahoo.inmind.your_app.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yahoo.inmind.model.NewsArticle;
import com.yahoo.inmind.your_app.R;

import java.util.ArrayList;

/**
 * Created by oscarr on 1/5/15.
 */
public class ListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private ArrayList elements = null;
    private int layoutId;

    // this improves the performance and reuse resources
    static class ViewHolder {
        public TextView text;
    }

    public ListAdapter(Context context, int layoutId, ArrayList<String> elements) {
        super(context, layoutId, elements);
        this.context = context;
        this.elements = elements;
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(layoutId, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.app_textViewItem);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        if( elements.get(position) instanceof String ) {
            String s = (String) elements.get(position);
            holder.text.setText(s);
        }else if( elements.get(position) instanceof NewsArticle){
            NewsArticle ni = (NewsArticle) elements.get(position);
            holder.text.setText( ni.getTitle() + "\n" + ni.getSummary() + "\n");
        }
        return rowView;
    }
}
