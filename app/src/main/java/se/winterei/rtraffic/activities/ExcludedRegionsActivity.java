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

public class ExcludedRegionsActivity extends BaseActivity{


    private List<Point> pointList;
    private ListView listView;
    private ArrayList<String> mobileArray = new ArrayList<String>();

    private boolean[] toggleSwitchStates= new boolean[100];

    ToggleButton togg;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded_regions);
        pointList = (List<Point>)appContext.get("MainMapPointList");

        if(savedInstanceState !=null){
            toggleSwitchStates = savedInstanceState.getBooleanArray("toggleSwitchStates");
        }else{
            Arrays.fill(toggleSwitchStates,Boolean.FALSE);
        }



        OnItemClickListener itemClickListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
                ListView lView = (ListView)lv;
                SimpleAdapter adapter =(SimpleAdapter) lView.getAdapter();
                HashMap<String,Object> hm = (HashMap) adapter.getItem(position);

                 //The clicked Item in the ListView
                //TextView rLayout = (TextView)item;
                LinearLayout rLayout = (LinearLayout) item;

                 //Getting the toggle button corresponding to the clicked item
                ToggleButton tgl = (ToggleButton) rLayout.getChildAt(1);

                String strStatus;

                if(tgl.isChecked()){

                    tgl.setChecked(false);

                    strStatus = "Off";

                    toggleSwitchStates[position]=false;

                }else{

                    tgl.setChecked(true);

                    strStatus = "On";

                    toggleSwitchStates[position]=true;

                }
                Toast.makeText(getBaseContext(), hm.get("txt") + " : " + strStatus, Toast.LENGTH_SHORT).show();
            }
        };

        for(int i = 0 ; i < pointList.size();i++){
            String cur = "";
            cur += pointList.get(i).title;
            /*cur += " , ( ";
            cur += pointList.get(i).latitude;
            cur += " , ";
            cur += pointList.get(i).longitude;
            cur += " )";*/
            mobileArray.add(cur);
        }

        // Each row in the list stores country name and its status

        ArrayList<HashMap<String, Object>> aList = new ArrayList<>();

        for(int i=0;i<mobileArray.size();i++){

            HashMap<String, Object> hm = new HashMap<>();

            hm.put("txt", mobileArray.get(i));

            hm.put("stat",toggleSwitchStates[i]);

            aList.add(hm);

        }

        // Keys used in Hashmap

        String[] from = {"txt","stat" };

        // Ids of views in listview_layout

        int[] to = { R.id.excludedListViewLabel, R.id.listViewTextViewToggleButton};

        // Instantiating an adapter to store each items

        // R.layout.listview_layout defines the layout of each item

        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.activity_excluded_listview, from, to);

        //ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.activity_excluded_listview,R.id.excludedListViewLabel, mobileArray);
        listView = (ListView)findViewById(R.id.excludedRegionsListView);
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

    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBooleanArray("toggleSwitchStates", toggleSwitchStates);

    }


}
