package se.winterei.rtraffic.libs.api;

import java.util.List;

/**
 * Created by reise on 3/4/2017.
 */

public class GenericAPIResponse
{
    public int status;
    public String message;
    public APIData data;

    public String toString ()
    {
        return "status: " + status + ", message: " + message;
    }
}
