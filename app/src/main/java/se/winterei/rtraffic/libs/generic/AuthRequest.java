package se.winterei.rtraffic.libs.generic;

/**
 * Created by reise on 3/25/2017.
 */

public class AuthRequest
{
    public String googleSignInToken;

    public String firebaseToken;

    public String provider;

    public AuthRequest (String googleSignInToken, String firebaseToken, String provider)
    {
        this.googleSignInToken = googleSignInToken;
        this.firebaseToken = firebaseToken;
        this.provider = provider;
    }
}
