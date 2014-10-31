package eu.corre.minder;

import eu.corre.minder.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener,
		ConnectionCallbacks, OnConnectionFailedListener {
	private static final String TAG = "LoginActivity";
	private static final int RC_SIGN_IN = 0;
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private boolean signInClicked;
	private boolean intentInProgess;

	private ConnectionResult connectionResult;
	private GoogleApiClient googleApiClient;

	private String accountName = "";
	private String name = "";

	private SignInButton btnSignIn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		if (checkPlayServices()) {
			googleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).addApi(Plus.API)
					.addScope(Plus.SCOPE_PLUS_PROFILE).build();

			btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
			btnSignIn.setOnClickListener(this);
		} else {
			Toast.makeText(this, "No valid Google Play Services APK found.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		googleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();

		if (googleApiClient.isConnected()) {
			googleApiClient.disconnect();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.sign_in_button && !googleApiClient.isConnecting()) {
			signInClicked = true;
			resolveSignInError();
		}
	}

	private void resolveSignInError() {
		if (connectionResult.hasResolution()) {
			try {
				intentInProgess = true;
				startIntentSenderForResult(connectionResult.getResolution()
						.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				intentInProgess = false;
				googleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!intentInProgess) {
			connectionResult = result;

			if (signInClicked) {
				resolveSignInError();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			if (responseCode != RESULT_OK) {
				signInClicked = false;
			}

			intentInProgess = false;

			if (!googleApiClient.isConnecting()) {
				googleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		signInClicked = false;

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.getBoolean("logout") == true) {
			if (googleApiClient.isConnected()) {
				Plus.AccountApi.clearDefaultAccount(googleApiClient);
				googleApiClient.disconnect();
				finish();
				getIntent().removeExtra("logout");
				startActivity(getIntent());
			}
		} else {
			accountName = Plus.AccountApi.getAccountName(googleApiClient);
			GeneralSettings.email = accountName;
			if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
				Person currentPerson = Plus.PeopleApi
						.getCurrentPerson(googleApiClient);
				name = currentPerson.getDisplayName();
			}
			GeneralSettings.userName = name;
			GeneralSettings.userID = accountName;
			Intent intent = new Intent(this, Homepage.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		googleApiClient.connect();
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Toast.makeText(this, "This device is not supported.",
						Toast.LENGTH_LONG).show();
				finish();
			}
			return false;
		}
		return true;
	}
}
