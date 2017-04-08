package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.generic.PointOfInterest;


public class PointsOfInterestActivity extends BaseActivity
{
    private final static String TAG = PointsOfInterestActivity.class.getSimpleName();

    private PointsOfInterestActivity instance = this;
    private List<Point> pointList;
    private ListView listView;

    private boolean[] toggleSwitchStates;

    private SimpleListViewAdapter adapter;
    private SparseArray<HashMap<String, Object>> pointSparseArray;

    public ProgressDialog progressDialog;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_of_interest);
        setupToolbar(null);
        setupNavigationView();

        pointSparseArray = new SparseArray<>();

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

        List<HashMap<String, Object>> aList = new ArrayList<>();

        int i = 0;
        for (final Point point : pointList)
        {
            final HashMap<String, Object> hm = new HashMap<>();
            hm.put("txt", point.title);
            hm.put("stat", toggleSwitchStates[i]);
            hm.put("point_id", point.id);
            hm.put("index_position", i);
            if(point.id != -1)
                pointSparseArray.append(point.id, hm);
            aList.add(hm);
            i++;
        }

        // Instantiating an adapter to store each items
        adapter = new SimpleListViewAdapter(getBaseContext(), aList, instance);
        listView = (ListView) findViewById(R.id.PointsOfInterestRegionsListView);
        listView.setAdapter(adapter);

        updatePoIStates();
    }

    @SuppressWarnings("unchecked")
    private void updatePoIStates ()
    {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        Call<List<PointOfInterest>> call = api.getPointsOfInterest();
        call.enqueue(new Callback<List<PointOfInterest>>()
        {
            @Override
            public void onResponse(Call<List<PointOfInterest>> call, Response<List<PointOfInterest>> response)
            {
                progressDialog.dismiss();
                List<PointOfInterest> points = response.body();
                if (points == null || points.size() == 0)
                {
                    Log.d(TAG, "onResponse: empty dataset found or it was null.");
                    return;
                }

                //Log.d(TAG, "onResponse: " + points.size() + " values returned by API.");

                for (PointOfInterest point : points)
                {
                    final HashMap<String, Object> map = pointSparseArray.get(point.point_id);
                    if(map == null)
                    {
                        Log.d(TAG, "onResponse: could not match PoI to Point :(");
                        continue;
                    }

                    map.put("stat", true);
                    ToggleButton toggle = (ToggleButton) map.get("toggle");
                    if(toggle != null)
                        toggle.setChecked(true);
                    int indexPosition = (Integer) map.get("index_position");
                    toggleSwitchStates[indexPosition] = true;
                }
            }

            @Override
            public void onFailure(Call<List<PointOfInterest>> call, Throwable t)
            {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: " + t.getMessage());
                showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
            }
        });
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

    @Override
    public void onDestroy ()
    {
        if (progressDialog != null)
            progressDialog.dismiss();
        super.onDestroy();
    }

}

