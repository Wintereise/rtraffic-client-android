package se.winterei.rtraffic.activities;

import android.database.MatrixCursor;
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
import java.util.Locale;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.map.MapContainer;
import se.winterei.rtraffic.libs.search.SearchFeedResultsAdapter;
public class PointsOfInterestActivity extends BaseActivity
{
    private SearchFeedResultsAdapter searchFeedResultsAdapter;
    private MapContainer mapContainer;
    private final HashMap<Integer, Point> searchPositionPoints = new HashMap<>();
    private final String[] columns = new String[]{"_id", "title", "position"};

    private PointsOfInterestActivity instance = this;
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

        // Keys used in Hashmap
        final String[] from = { "txt", "stat" };

        // Ids of views in listview_layout
        final int[] to = { R.id.excludedListViewLabel, R.id.listViewTextViewToggleButton };

        // Instantiating an adapter to store each items
        final SimpleListViewAdapter adapter = new SimpleListViewAdapter(getBaseContext(),aList);
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
                                filterMarkers(query);
                                return true;
                            }

                            @Override
                            public boolean onQueryTextChange (String query)
                            {
                                if (query.length() >= 3)
                                {
                                    filterMarkers(query);
                                }
                                return true;
                            }
                        }
                );

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionSelect(int position)
            {
                //focusMarker(searchView, position);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position)
            {
                //focusMarker(searchView, position);
                return true;
            }
        });

        searchFeedResultsAdapter = new SearchFeedResultsAdapter(instance, R.layout.search_suggestions, null, columns, null, -1000);

        searchView.setSuggestionsAdapter(searchFeedResultsAdapter);
    }

//    private void focusMarker (SearchView searchView, int position)
//    {
//        Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
//        int index = Integer.parseInt(cursor.getString(2));
//
//        Marker marker = searchPositionMap.get(index);
//        if(marker != null)
//        {
//            mapContainer.getMap()
//                    .animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
//            marker.showInfoWindow();
//            searchView.setQuery(marker.getTitle(), false);
//        }
//
//        searchView.clearFocus();
//    }

    private void filterMarkers (String searchText)
    {
        List<Point> results;
        searchText = searchText.toLowerCase(Locale.getDefault());

        if (searchText.isEmpty())
        {
            results = pointList;
        }
        else
        {
            results = new ArrayList<>();
            for (Point point : pointList)
            {

                if(point.getTitle().toLowerCase(Locale.getDefault()).contains(searchText))
                {
                  results.add(point);
                }
            }
        }

        if (results.size() > 0)
        {
            MatrixCursor matrixCursor = new MatrixCursor(columns);
            searchPositionPoints.clear();
            int index = 0;
            for (Point point : results)
            {
                String[] tmp = new String[]{ Integer.toString(0), point.getTitle(), Integer.toString(index) };
                searchPositionPoints.put(index, point);
                matrixCursor.addRow(tmp);
                index++;
            }
            searchFeedResultsAdapter.changeCursor(matrixCursor);
        }

    }




}

