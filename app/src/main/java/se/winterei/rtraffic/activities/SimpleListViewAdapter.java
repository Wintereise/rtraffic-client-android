package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.api.GenericAPIResponse;
import se.winterei.rtraffic.libs.generic.PointOfInterest;

public class SimpleListViewAdapter extends BaseAdapter{

    Context context;
    private List<HashMap<String, Object>> data;
    private ArrayList< HashMap<String,Object>> arrayList;

    private PointsOfInterestActivity activity;
    private Toast toast;

    private final static String TAG = SimpleListViewAdapter.class.getSimpleName();


    public SimpleListViewAdapter(Context context, List<HashMap<String, Object>> data, PointsOfInterestActivity activity)
    {
        super();
        this.context=context;
        this.data=data;

        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(data);

        this.activity = activity;

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

    @SuppressWarnings("unchecked")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
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

        final HashMap<String, Object> tmp = data.get(position);

        holderView.aToggle.setChecked((Boolean) tmp.get("stat"));
        holderView.aText.setText((String) tmp.get("txt"));
        holderView.aToggle.setTag(tmp);

        tmp.put("toggle", holderView.aToggle);

        holderView.aToggle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                final ToggleButton  toggleButton  = (ToggleButton) v;
                final HashMap<String, Object> row = (HashMap<String, Object>) v.getTag();

                activity.progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.loading), true);
                activity.progressDialog.show();

                final int point_id = (Integer) row.get("point_id");
                Call<GenericAPIResponse> call;

                if(toggleButton.isChecked())
                    call = activity.api.postPointOfInterest(new PointOfInterest(-1, -1, point_id));
                else
                {
                    call = activity.api.deletePointOfInterest(point_id);
                }


                call.enqueue(new Callback<GenericAPIResponse>()
                {
                    @Override
                    public void onResponse(Call<GenericAPIResponse> call, Response<GenericAPIResponse> response)
                    {
                        activity.progressDialog.dismiss();
                        if (response.isSuccessful())
                        {
                            if (toast != null)
                                toast.cancel();

                            if(toggleButton.isChecked())
                            {
                                toast = activity.showToast(R.string.entry_submit, Toast.LENGTH_SHORT);
                                toggleButton.setChecked(true);
                                data.get(position).put("stat", true);//this is imp to update the value in dataset which is provided to listview
                            }
                            else
                            {
                                toast = activity.showToast(R.string.excluded_regions_successful_deletion, Toast.LENGTH_SHORT);
                                toggleButton.setChecked(false);
                                data.get(position).put("stat", false);
                            }

                        }

                        else if (response.code() == 404)
                        {
                            activity.showToast(R.string.excluded_regions_successful_deletion, Toast.LENGTH_SHORT);
                            toggleButton.setChecked(false);
                            data.get(position).put("stat", false);
                        }

                        else if (! response.isSuccessful() && response.body() != null)
                            activity.showToast(response.body().message, Toast.LENGTH_SHORT);
                        else
                            activity.showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onFailure(Call<GenericAPIResponse> call, Throwable t)
                    {
                        activity.progressDialog.dismiss();
                        activity.showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                });

            }
        });
        return convertView;
    }

    static class HolderView{
        TextView aText;
        ToggleButton aToggle;
    }

    public void filter(String searchText)
    {
        data.clear();
        if(searchText.length() == 0)
        {
            data.addAll(arrayList);
        }
        else
        {
            for (HashMap<String,Object> u: arrayList)
            {
                if( ((String) u.get("txt")).toLowerCase(Locale.getDefault()).contains(searchText))
                    data.add(u);
            }
        }
        notifyDataSetChanged();
    }
}