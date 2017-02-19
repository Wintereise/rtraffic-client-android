package se.winterei.rtraffic.libs.search;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import se.winterei.rtraffic.R;

/**
 * Created by reise on 2/19/2017.
 */

public class SearchFeedResultsAdapter
        extends SimpleCursorAdapter
{
    private static final String tag = SearchFeedResultsAdapter.class.getName();
    private Context context = null;

    public SearchFeedResultsAdapter (Context context, int layout, Cursor c, String[] from, int[] to, int flags)
    {
        super(context, layout, c, from, to, flags);
        this.context = context;
    }

    @Override
    public void bindView (View view, Context context, Cursor cursor)
    {
        ImageView imageView = (ImageView) view.findViewById(R.id.search_icon);
        TextView textView = (TextView) view.findViewById(R.id.search_location_name);
        imageView.setImageResource(R.drawable.ic_explore_black_24dp);
        textView.setText(cursor.getString(1));
    }
}
