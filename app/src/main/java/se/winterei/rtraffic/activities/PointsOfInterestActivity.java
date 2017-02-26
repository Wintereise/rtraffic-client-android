package se.winterei.rtraffic.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Point;

public class PointsOfInterestActivity extends BaseActivity
{

    private List<Point> pointList;
    private ListView listView;

    private final List<String> mobileArray = new ArrayList<>();
    private boolean[] toggleSwitchStates;
    private Toast toast;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_of_interest);

        pointList = (List<Point>) appContext.get("MainMapPointList");
        toggleSwitchStates = new boolean[pointList.size()];

        if (savedInstanceState != null)
        {
            toggleSwitchStates = savedInstanceState.getBooleanArray("toggleSwitchStates");
        }
        else
        {
            Arrays.fill(toggleSwitchStates, Boolean.FALSE);
        }

        OnItemClickListener itemClickListener = new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id)
            {
                final ListView lView = (ListView) lv;
                final SimpleAdapter adapter =(SimpleAdapter) lView.getAdapter();
                final HashMap<String,Object> hm = (HashMap) adapter.getItem(position);

                //The clicked Item in the ListView
                //TextView rLayout = (TextView)item;
                final LinearLayout rLayout = (LinearLayout) item;

                //Getting the toggle button corresponding to the clicked item
                final ToggleButton tgl = (ToggleButton) rLayout.getChildAt(1);

                String strStatus;

                if (tgl.isChecked())
                {
                    tgl.setChecked(false);
                    strStatus = "Off";
                    toggleSwitchStates[position] = false;
                }
                else
                {
                    tgl.setChecked(true);
                    strStatus = "On";
                    toggleSwitchStates[position] = true;
                }
                if (toast != null)
                    toast.cancel();
                toast = showToast(hm.get("txt") + ": " + strStatus, Toast.LENGTH_SHORT);
            }
        };

        for (final Point point : pointList)
        {
            mobileArray.add(point.title);
        }

        List<HashMap<String, Object>> aList = new ArrayList<>();


        for (int i=0; i < mobileArray.size(); i++)
        {
            final HashMap<String, Object> hm = new HashMap<>();
            hm.put("txt", mobileArray.get(i));
            hm.put("stat",toggleSwitchStates[i]);
            aList.add(hm);
        }

        // Keys used in Hashmap
        final String[] from = { "txt", "stat" };

        // Ids of views in listview_layout
        final int[] to = { R.id.excludedListViewLabel, R.id.listViewTextViewToggleButton };

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
        final SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.activity_points_of_interest_listview, from, to);

        //ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.activity_points_of_interest_listview,R.id.excludedListViewLabel, mobileArray);
        listView = (ListView) findViewById(R.id.PointsOfInterestRegionsListView);
        listView.setOnItemClickListener(itemClickListener);
        listView.setAdapter(adapter);

        setupToolbar(null);
        setupNavigationView();
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

    /** Saving the current state of the activity

     * for configuration changes [ Portrait <=> Landscape ]

     */

    @Override

    protected void onSaveInstanceState(Bundle outState)
    {

        super.onSaveInstanceState(outState);
        outState.putBooleanArray("toggleSwitchStates", toggleSwitchStates);
    }


}
