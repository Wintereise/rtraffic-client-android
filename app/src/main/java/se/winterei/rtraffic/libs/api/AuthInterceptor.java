package se.winterei.rtraffic.libs.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by reise on 2/24/2017.
 */

public class AuthInterceptor implements Interceptor
{
    private String key;

    public AuthInterceptor (String key)
    {
        this.key = key;
    }

    @Override
    public Response intercept (Chain chain) throws IOException
    {
        Request request = chain.request()
                .newBuilder()
                .addHeader("X-RTRAFFIC-KEY", key)
                .build();
        return chain.proceed(request);
    }
}
