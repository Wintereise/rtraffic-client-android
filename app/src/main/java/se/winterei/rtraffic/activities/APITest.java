package se.winterei.rtraffic.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.api.APIClient;
import se.winterei.rtraffic.libs.api.APIInterface;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.generic.Report;
import se.winterei.rtraffic.libs.generic.Utility;

public class APITest extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apitest);

       APIInterface apiInterface = APIClient.get()
                .create(APIInterface.class);

        List<LatLng> latLngList = new ArrayList<>();
        latLngList.add(new LatLng(23.0051, 45.00121));
        latLngList.add(new LatLng(24.0042, 70.1121));

        Report report = new Report(-1, Utility.CONGESTED, "Herpa la derpa", true, latLngList);



        Call<Report> call = apiInterface.postReport(report);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                Log.d("APIDBG", "Run successful!");
            }

            @Override
            public void onFailure(Call<Report> call, Throwable t) {
                Log.d("APIDBG", "Run failed!");
            }
        });



    }
}
