package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.api.APIData;
import se.winterei.rtraffic.libs.api.GenericAPIResponse;
import se.winterei.rtraffic.libs.generic.AuthRequest;
import se.winterei.rtraffic.libs.generic.Utility;

public class SophisticatedSignIn extends BaseActivity
    implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener
{
    private VideoView videoView;
    private int currentSeekPosition = -1;
    private final String TAG = SophisticatedSignIn.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private ProgressDialog mProgressDialog;
    private RTraffic appContext;
    private final String provider = "GOOGLE";
    private Button signInButton;
    private TextView rTrafficBanner;

    public SophisticatedSignIn ()
    {
        this.bypassAuthentication = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sophisticated_sign_in);

        videoView = (VideoView) findViewById(R.id.bgVideoView);
        rTrafficBanner = (TextView) findViewById(R.id.rtraffic_login_banner);
        rTrafficBanner.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/print_clearly.otf"), Typeface.NORMAL);

        Uri uri  = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.city_traffic);

        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                mp.setLooping(true);
                mp.setScreenOnWhilePlaying(false);
            }
        });

        appContext = (RTraffic) getApplicationContext();

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_oauth_web_client_id))
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]
    }

    @Override
    public void onStart()
    {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        showProgressDialog();
        if (opr.isDone())
        {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");

            if (signInButton != null)
            {
                signInButton.setEnabled(false);
                signInButton.setClickable(false);
            }

            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        }
        else
        {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>()
            {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult)
                {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    private void handleApiSignIn (final GoogleSignInAccount acct)
    {
        String googleIdToken = acct.getIdToken();
        String firebaseIdToken = FirebaseInstanceId.getInstance().getToken();

        if (googleIdToken == null)
        {
            Log.d(TAG, "handleApiSignIn: Google Sign-in token was null, cannot proceed.");
            return;
        }
        if (firebaseIdToken == null)
        {
            firebaseIdToken = "";
            Log.d(TAG, "handleApiSignIn: Firebase ID token was null, re-initialized with empty string.");
        }

        Call<GenericAPIResponse> call = api.authRequest(new AuthRequest(googleIdToken, firebaseIdToken, this.provider));
        call.enqueue(new Callback<GenericAPIResponse>()
        {
            @Override
            public void onResponse(Call<GenericAPIResponse> call, Response<GenericAPIResponse> response)
            {
                hideProgressDialog();
                GenericAPIResponse apiResponse = response.body();
                if (apiResponse != null)
                {
                    if (apiResponse.status == 200)
                    {
                        APIData apiData = apiResponse.data;

                        if (apiData != null)
                        {
                            preference.put(Utility.RTRAFFIC_API_KEY, apiData.token, String.class);
                            startActivity(new Intent(SophisticatedSignIn.this, MainActivity.class));
                        }
                        else
                        {
                            showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                            Log.d(TAG, "onResponse: apiData was null :T");
                        }
                    }
                    else
                        showToast(response.body().message, Toast.LENGTH_SHORT);
                }
                else
                {
                    Log.d(TAG, "onResponse: apiResponse was null :T");
                    showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                }

            }

            @Override
            public void onFailure(Call<GenericAPIResponse> call, Throwable t)
            {
                hideProgressDialog();
                showToast(R.string.err_inet_could_not_connect, Toast.LENGTH_LONG);
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result)
    {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        appContext.put("GSignInResult", result);
        if (result.isSuccess())
        {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();

            if (acct == null)
            {
                showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                Log.d(TAG, "handleSignInResult: acct was null");
                return;
            }
            handleApiSignIn(acct);
        }
        else
            hideProgressDialog();
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    @Override
    public void onPause ()
    {
        if (videoView != null && videoView.isPlaying())
        {
            videoView.pause();
            currentSeekPosition = videoView.getCurrentPosition();
        }
        super.onPause();
    }

    @Override
    public void onResume ()
    {
        if (videoView != null && currentSeekPosition != -1)
        {
            videoView.seekTo(currentSeekPosition);
            videoView.start();
        }
        super.onResume();
    }

    private void showProgressDialog()
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onDestroy ()
    {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        super.onDestroy();
    }
}
