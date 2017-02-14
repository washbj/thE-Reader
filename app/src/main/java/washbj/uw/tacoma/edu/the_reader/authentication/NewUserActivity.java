package washbj.uw.tacoma.edu.the_reader.authentication;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.functionality.ReadActivity;

public class NewUserActivity extends AppCompatActivity {
    /** The URL of the database we're pulling passwords from. */
    private static final String USERS_URL = "http://cssgate.insttech.washington.edu/~_450bteam5/list.php?cmd=users";

    /** A URL for adding new users. */
    private static final String ADD_USER_URL = "http://cssgate.insttech.washington.edu/~_450bteam5/addUser.php?";

    /** The username for the new user. */
    private String mUsername;

    /** The password for the new user. */
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        final EditText fieldUsername = (EditText) findViewById(R.id.new_username);
        final EditText fieldPassword = (EditText) findViewById(R.id.new_password);
        Button buttonAddUser = (Button) findViewById(R.id.button_add_user);

        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsername = fieldUsername.getText().toString();
                mPassword = fieldPassword.getText().toString();

                // Test entered data across all spectrums.
                if (TextUtils.isEmpty(mUsername)) {
                    Toast.makeText(v.getContext(), "Enter a Username!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    fieldUsername.requestFocus();
                    return;
                } else if (mUsername.contains("\\s")) {
                    Toast.makeText(v.getContext(), "Username cannot contain whitespace!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    return;
                } else if (mUsername.length() > 32) {
                    Toast.makeText(v.getContext(), "Username cannot be more than 32 characters in length!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    return;
                } else if (TextUtils.isEmpty(mPassword)) {
                    Toast.makeText(v.getContext(), "Enter a Password!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    fieldPassword.requestFocus();
                    return;
                } else if (mPassword.contains("\\s")) {
                    Toast.makeText(v.getContext(), "Password cannot contain whitespace!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    return;
                } else {
                    VerifyUserIsUniqueTask task = new VerifyUserIsUniqueTask();
                    task.execute(new String[]{USERS_URL});

                }

            }
        });

    }


    /**
     * Downloads a list of all users and their associated passwords, then attempts to verify if the
     * current username is unique.
     */
    private class VerifyUserIsUniqueTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            // Attempt to download user information from the database.
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    response = "Unable to download user database, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }

            return response;

        }

        @Override
        protected void onPostExecute(String result) {
            // Something is wrong with the network or the URL.
            if (result.startsWith("Unable to")) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            HashMap<String, String> hmUserMap = new HashMap<String, String>();
            result = LoginFragment.parseUsersJSON(result, hmUserMap);

            // Something is wrong with the JSON returned.
            if (result != null) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // Checks to see if the user already exists in the database.
            if (hmUserMap.containsKey(mUsername)) {
                Toast.makeText(getApplicationContext(),
                        "User already exists!",
                        Toast.LENGTH_SHORT).show();

            } else {
                // If not...
                StringBuilder sbURL = new StringBuilder(ADD_USER_URL);
                try {
                    sbURL.append("username=");
                    sbURL.append(mUsername);
                    sbURL.append("&password=");
                    sbURL.append(mPassword);

                }
                catch(Exception e) {
                    Toast.makeText(getApplicationContext(), "Something wrong with the url" + e.getMessage(), Toast.LENGTH_LONG)
                            .show();

                }

                // Begin new task. This one must add a new user.
                AddUserTask task = new AddUserTask();
                task.execute(new String[]{sbURL.toString()});

            }
        }

    }


    /**
     * Adds the current username/password combination to the database, exiting the Activity afterwards.
     */
    private class AddUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    response = "Unable to add user, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }


        @Override
        protected void onPostExecute(String result) {
            // Something was wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getApplicationContext(), "User added successfully!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to add: "
                                    + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something wrong with the data" +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
