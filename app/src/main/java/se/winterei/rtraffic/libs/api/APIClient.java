package se.winterei.rtraffic.libs.api;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Created by reise on 2/24/2017.
 */

public class APIClient
{
    private static final String BASE_URL = "http://192.168.1.150/api/";
    private static Retrofit retrofit = null;

    public static Retrofit get ()
    {
        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
