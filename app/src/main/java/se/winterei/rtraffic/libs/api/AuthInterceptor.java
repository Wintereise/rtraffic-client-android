package se.winterei.rtraffic.libs.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.settings.Preference;

/**
 * Created by reise on 2/24/2017.
 */

public class AuthInterceptor implements Interceptor
{
    private Preference preference;

    public AuthInterceptor ()
    {
        preference = new Preference(RTraffic.getAppContext());
    }

    @Override
    public Response intercept (Chain chain) throws IOException
    {
        Request request = chain.request()
                .newBuilder()
                .addHeader("X-RTRAFFIC-KEY", (String) preference.get(Utility.RTRAFFIC_API_KEY, "", String.class))
                .build();
        return chain.proceed(request);
    }
}
