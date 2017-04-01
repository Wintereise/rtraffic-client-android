package se.winterei.rtraffic.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.CommentData;
import se.winterei.rtraffic.libs.generic.Utility;

public class CommentOverlay extends BaseActivity
{

    @Override
    @SuppressWarnings({"unchecked"})
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_overlay);

        setupToolbar(null);
        setupNavigationView();

        Intent intent = getIntent();
        ArrayList<CommentData> commentDataArrayList = (ArrayList<CommentData>) intent.getSerializableExtra("data");

        if (commentDataArrayList == null || commentDataArrayList.size() <= 0)
            finish();

        RelativeLayout base = (RelativeLayout) findViewById(R.id.relative_container);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout childLayoutRight = (LinearLayout) inflater.inflate(R.layout.comment_skel_right, base, false);
        LinearLayout childLayoutLeft = (LinearLayout) inflater.inflate(R.layout.comment_skel_left, base, false);

        int turnTracker = 0;
        int lastId = -1;

        for (CommentData commentData : commentDataArrayList)
        {
            final LinearLayout layout;
            if (turnTracker % 2 == 0)
                layout  = childLayoutRight;
            else
                layout = childLayoutLeft;

            TextView textView = (TextView) layout.findViewById(R.id.comment_overlay_textview);
            textView.setText(commentData.comment);

            if (turnTracker != 0)
            {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(layout.getLayoutParams());
                layoutParams.addRule(RelativeLayout.BELOW, lastId);
                layoutParams.setMargins(0, Utility.dpToPx(this, 25), 0, 0);
                layout.setLayoutParams(layoutParams);
            }

            lastId = View.generateViewId();
            layout.setId(lastId);
            base.addView(layout);

            turnTracker++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        genericFixToolbar(menu);

        return true;
    }

    @Override
    public void onBackPressed ()
    {
        finish();
    }
}
