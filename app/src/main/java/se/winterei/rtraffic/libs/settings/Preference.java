package se.winterei.rtraffic.libs.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by reise on 3/17/2017.
 */

public class Preference
{
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public Preference (Context context)
    {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
    }

    public Object get (String key, Object def, Class<?> clazz)
    {
        Object ret = null;
        switch (clazz.getSimpleName())
        {
            case "string":
            case "String":
                ret = preferences.getString(key, (String) def);
                break;
            case "boolean":
            case "Boolean":
                ret = preferences.getBoolean(key, (Boolean) def);
                break;
        }
        return ret;
    }

    public void put (String key, Object value, Class<?> clazz)
    {
        switch (clazz.getSimpleName())
        {
            case "string":
            case "String":
                editor.putString(key, (String) value);
                break;
            case "boolean":
            case "Boolean":
                editor.putBoolean(key, (Boolean) value);
                break;
        }
        editor.commit();
    }
}
