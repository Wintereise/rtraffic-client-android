package se.winterei.rtraffic.activities;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.search.SearchFeedResultsAdapter;


public class PointsOfInterestActivity extends BaseActivity
{
    private SearchFeedResultsAdapter searchFeedResultsAdapter;
    private final String[] columns = new String[]{"_id", "title", "position"};

    private PointsOfInterestActivity instance = this;
    private List<Point> pointList;
    private ListView listView;

    private final List<String> mobileArray = new ArrayList<>();
    private boolean[] toggleSwitchStates;

    SimpleListViewAdapter adapter;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_of_interest);
        setupToolbar(null);
        setupNavigationView();

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

        for (final Point point : pointList)
        {
            mobileArray.add(point.getTitle());
        }

        List<HashMap<String, Object>> aList = new ArrayList<>();


        for (int i=0; i < mobileArray.size(); i++)
        {
            final HashMap<String, Object> hm = new HashMap<>();
            hm.put("txt", mobileArray.get(i));
            hm.put("stat",toggleSwitchStates[i]);
            aList.add(hm);
        }

        // Instantiating an adapter to store each items
        adapter = new SimpleListViewAdapter(getBaseContext(),aList);
        listView = (ListView) findViewById(R.id.PointsOfInterestRegionsListView);
        listView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //super.onCreateOptionsMenu(menu);
        //genericFixToolbar(menu);
        getMenuInflater().inflate(R.menu.main_titlebar, menu);
        setupSearchBar(menu);

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


    public final void setupSearchBar (Menu menu)
    {
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();

        searchView.setOnQueryTextListener
        (
                new SearchView.OnQueryTextListener ()
                {
                    @Override
                    public boolean onQueryTextSubmit (String query)
                    {
                        adapter.filter(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange (String query)
                    {
                        adapter.filter(query);
                        return true;
                    }
                }
        );
    }

}

