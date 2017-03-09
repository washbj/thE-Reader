package washbj.uw.tacoma.edu.the_reader.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.functionality.ShelfActivity;

/**
 * A shell activity for holding LoginFragment.
 */
public class LoginActivity extends AppCompatActivity
        implements LoginFragment.LoginInteractionListener {

    /** The SharedPreferences dedicated to holding login information. */
    private SharedPreferences mSharedPreferences;

    /**
     * Checks if the user is logged in. If so, pass them straight on through
     * to the Shelf Activity. Also checks to ensure the app can connect to
     * the database.
     *
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);
        if (!mSharedPreferences.getBoolean(getString(R.string.LOGGEDIN), false)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_login, new LoginFragment())
                    .commit();
        } else {
            Intent i = new Intent(this, ShelfActivity.class);
            startActivity(i);
            finish();
        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (!(networkInfo != null && networkInfo.isConnected())) {
            Toast.makeText(getApplicationContext(),
                    "No network connection available. Don't bother entering any information, as I won't be able to authenticate it.",
                    Toast.LENGTH_LONG).show();

        }

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
