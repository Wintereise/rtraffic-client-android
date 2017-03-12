package se.winterei.rtraffic.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import se.winterei.rtraffic.R;

public class SimpleListViewAdapter extends BaseAdapter{

    Context context;
    private List<HashMap<String, Object>> data;
    private ArrayList< HashMap<String,Object>> arrayList;


    public SimpleListViewAdapter(Context context, List<HashMap<String, Object>> data) {
        super();
        this.context=context;
        this.data=data;

        this.arrayList = new ArrayList<HashMap<String,Object>>();
        this.arrayList.addAll(data);

    }

    @Override
    public Map<String,Object> getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        HolderView holderView;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.points_listview,parent, false);
            holderView = new HolderView();
            holderView.aToggle  = (ToggleButton) convertView.findViewById(R.id.listViewTextViewToggleButton);
            holderView.aText = (TextView)convertView.findViewById(R.id.excludedListViewLabel);
            convertView.setTag(holderView);
        } else {
            // View recycled !
            // no need to inflate
            // no need to findViews by id
            holderView = (HolderView) convertView.getTag();
        }

        holderView.aToggle.setChecked((Boolean)data.get(position).get("stat"));
        holderView.aText.setText((String)data.get(position).get("txt"));
        holderView.aToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(((ToggleButton)v).isChecked()){
                    ((ToggleButton)v).setChecked(true);
                    data.get(position).put("stat",true);//this is imp to update the value in dataset which is provided to listview

                }else{
                    ((ToggleButton)v).setChecked(false);
                    data.get(position).put("stat",false);

                }
            }
        });
        return convertView;
    }

    static class HolderView{
        TextView aText;
        ToggleButton aToggle;
    }
    public void filter(String searchText){
        data.clear();
        if(searchText.length() == 0){
            data.addAll(arrayList);
        }
        else
        {
            for (HashMap<String,Object> u: arrayList){
                if( ((String)u.get("txt")).toLowerCase(Locale.getDefault()).contains(searchText)){
                    data.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }
}