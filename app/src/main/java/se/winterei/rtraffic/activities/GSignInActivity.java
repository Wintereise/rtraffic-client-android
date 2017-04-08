package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.api.APIData;
import se.winterei.rtraffic.libs.api.GenericAPIResponse;
import se.winterei.rtraffic.libs.generic.AuthRequest;
import se.winterei.rtraffic.libs.generic.Utility;

public class GSignInActivity extends BaseActivity implements
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = GSignInActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    private RTraffic appContext;

    private final String provider = "GOOGLE";

    private boolean contactAPI = true;
    public boolean startedMainActivity = false;

    public GSignInActivity ()
    {
        this.bypassAuthentication = true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsign_in);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        appContext = (RTraffic) getApplicationContext();

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

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

        // [START customize_button]
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        // [END customize_button]
    }

    @Override
    public void onStart()
    {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone())
        {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        }
        else
        {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>()
            {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult)
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

        if (googleIdToken == null || firebaseIdToken == null)
        {
            Log.d(TAG, "handleApiSignIn: one or more required resources were null.");
            return;
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
                            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
                            updateUI(true);
                            preference.put(Utility.RTRAFFIC_API_KEY, apiData.token, String.class);

                            if (!startedMainActivity)
                            {
                                startedMainActivity = true;
                                startActivity(new Intent(GSignInActivity.this, MainActivity.class));
                            }
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
                showToast(R.string.something_went_wrong, Toast.LENGTH_LONG);
                Log.d(TAG, "onFailure: " + t.getMessage());
                updateUI(false);
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
                Log.d(TAG, "handleSignInResult: GoogleSignInAccount was null :(");
                updateUI(false);
                return;
            }

            if(getIntent().getStringExtra("se.winterei.rtraffic.GSignInActivityFilter") == null)
            {
                if (contactAPI)
                {
                    handleApiSignIn(acct);
                    contactAPI = false;
                }
                else
                {
                    mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
                    updateUI(true);
                    if (!startedMainActivity)
                    {
                        startedMainActivity = true;
                        startActivity(new Intent(GSignInActivity.this, MainActivity.class));
                    }
                }
            }
            else
            {
                mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
                updateUI(true);
            }
        }
        else
        {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>()
                {
                    @Override
                    public void onResult(Status status)
                    {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess()
    {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
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

    private void updateUI(boolean signedIn)
    {
        if (signedIn)
        {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        }
        else
        {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }

    @Override
    public void onBackPressed ()
    {
        finish();
    }

    @Override
    public void onDestroy ()
    {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        super.onDestroy();
    }

}
