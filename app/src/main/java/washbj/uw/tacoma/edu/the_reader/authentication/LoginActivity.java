package washbj.uw.tacoma.edu.the_reader.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.functionality.ShelfActivity;

/**
 * A shell activity for holding LoginFragment.
 */
public class LoginActivity extends AppCompatActivity
        implements LoginFragment.LoginInteractionListener {
    private SharedPreferences mSharedPreferences;

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
