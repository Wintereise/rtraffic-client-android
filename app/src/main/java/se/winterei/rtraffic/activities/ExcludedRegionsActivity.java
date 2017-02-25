package se.winterei.rtraffic.activities;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Point;

public class ExcludedRegionsActivity extends BaseActivity
{

    private List<Point> pointList;
    private ListView listView;
    private ArrayList<String> mobileArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded_regions);
        pointList = (List<Point>)appContext.get("MainMapPointList");

        for(int i = 0 ; i < pointList.size();i++){
            String cur = "";
            cur += pointList.get(i).title;
            cur += " , ( ";
            cur += pointList.get(i).latitude;
            cur += " , ";
            cur += pointList.get(i).longitude;
            cur += " )";
            mobileArray.add(cur);
        }


        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_excluded_listview, mobileArray);

        listView = (ListView)findViewById(R.id.excludedRegionsListView);
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
}
