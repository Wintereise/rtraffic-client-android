package se.winterei.rtraffic;

import android.app.Application;

import java.util.HashMap;

/**
 * Created by reise on 1/22/2017.
 */

public class RTraffic extends Application
{
    private HashMap<String, Object> data = new HashMap<>();

    public Object get (String id)
    {
        return data.get(id);
    }

    public void put (String id, Object obj)
    {
        data.put(id, obj);
    }
}
