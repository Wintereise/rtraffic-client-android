package se.winterei.rtraffic.libs.api;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import se.winterei.rtraffic.BuildConfig;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.settings.Preference;

/**
 * Created by reise on 2/24/2017.
 */

public class APIClient
{
    private static final String BASE_URL = BuildConfig.API_ENDPOINT;

    private static Retrofit retrofit = null;
    //private static Preference preference = new Preference()

    public static Retrofit get ()
    {
        if (retrofit == null)
        {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
