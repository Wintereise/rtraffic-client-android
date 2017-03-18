package se.winterei.rtraffic.libs.generic;

/**
 * Created by reise on 3/18/2017.
 */

public class PointOfInterest
{
    public int user_id = -1;
    public int point_id = -11;
    public int id = -1;

    public PointOfInterest (int id, int uid, int pid)
    {
        this.user_id = uid;
        this.point_id = pid;
        this.id = id;
    }
}
