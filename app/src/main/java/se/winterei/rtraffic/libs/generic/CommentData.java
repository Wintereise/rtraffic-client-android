package se.winterei.rtraffic.libs.generic;

import java.io.Serializable;

/**
 * Created by reise on 4/1/2017.
 */

public class CommentData implements Serializable
{
    public String comment, created_at = "";

    public CommentData (String comment, String created_at)
    {
        this.comment = comment;
        this.created_at = created_at;
    }
}
