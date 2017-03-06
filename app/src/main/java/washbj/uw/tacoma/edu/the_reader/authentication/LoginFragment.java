package washbj.uw.tacoma.edu.the_reader.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.functionality.ShelfActivity;


/**
 * A fragment which allows the user to log into the app with a username/password.
 * Checks the usernames/passwords against an online database to verify them.
 */
public class LoginFragment extends Fragment {
    /** The URL of the database we're pulling passwords from. */
    private static final String USERS_URL = "http://cssgate.insttech.washington.edu/~_450bteam5/list.php?cmd=users";

    /** Shared preferences. Self-explanatory. */
    private SharedPreferences mSharedPreferences;

    /** A HashMap for containing all username/password combinations. */
    private HashMap<String, String> mUserMap;

    /** Interaction listener. Not used at the moment. */
    private LoginInteractionListener mListener;

    /** Holds the currently typed username. */
    private String mUsername;

    /** Holds the currently typed password. */
    private String mPassword;


    /**
     * Empty constructor.
     */
    public LoginFragment() {
        // Required empty public constructor
    }


    /**
     * Initializes a new instance of this Fragment type.
     *
     * @return Returns a new Fragment of this type.
     */
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Arguments pending
        }

        mSharedPreferences = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final EditText fieldUsername = (EditText) view.findViewById(R.id.enter_username);
        final EditText fieldPassword = (EditText) view.findViewById(R.id.enter_password);
        Button buttonLogin = (Button) view.findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsername = fieldUsername.getText().toString();
                mPassword = fieldPassword.getText().toString();

                if (TextUtils.isEmpty(mUsername)) {
                    Toast.makeText(v.getContext(), "Enter a Username!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    fieldUsername.requestFocus();
                } else if (mUsername.contains("\\s")) {
                    Toast.makeText(v.getContext(), "Username cannot contain whitespace!"
                            , Toast.LENGTH_SHORT)
                            .show();
                } else if (mUsername.length() > 32) {
                    Toast.makeText(v.getContext(), "Username cannot be more than 32 characters in length!"
                            , Toast.LENGTH_SHORT)
                            .show();
                } else if (TextUtils.isEmpty(mPassword)) {
                    Toast.makeText(v.getContext(), "Enter a Password!"
                            , Toast.LENGTH_SHORT)
                            .show();
                    fieldPassword.requestFocus();
                } else if (mPassword.contains("\\s")) {
                    Toast.makeText(v.getContext(), "Password cannot contain whitespace!"
                            , Toast.LENGTH_SHORT)
                            .show();
                } else {
                    attemptLogin();

                }

            }
        });


        Button buttonNewUser = (Button) view.findViewById(R.id.button_new_user);
        buttonNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNewUser = new Intent(getActivity(), NewUserActivity.class);
                startActivity(intentNewUser);
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginInteractionListener) {
            mListener = (LoginInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * Attempts to log in using the current Username and Password combo.
     */
    public void attemptLogin() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadUsersTask task = new DownloadUsersTask();
            task.execute(new String[]{USERS_URL});
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "No network connection available. Unable to verify username/password.",
                    Toast.LENGTH_SHORT).show();

        }

    }


    /**
     * Parses the JSON string, returning an error message if unsuccessful.
     * Returns user/password keymap otherwise.
     *
     * @param usersJSON The input JSON string to parse.
     * @param usersMap The HashMap to output to.
     * @return Reason why if unsuccessful, or null if successful.
     */
    public static String parseUsersJSON(String usersJSON, HashMap<String, String> usersMap) {
        String reason = null;

        if (usersJSON != null) {
            try {
                JSONArray arr = new JSONArray(usersJSON);

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    usersMap.put(obj.getString("username"), obj.getString("password"));
                }
            } catch (JSONException e) {
                reason =  "Unable to parse data, Reason: " + e.getMessage();
            }

        }
        return reason;
    }

    /**
     * Downloads a list of all users and their associated passwords, then attempts to verify if the
     * current user/password combo is valid.
     */
    private class DownloadUsersTask extends AsyncTask<String, Void, String> {

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
            // Something wrong with the network or the URL.
            if (result.startsWith("Unable to")) {
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            mUserMap = new HashMap<String, String>();
            result = parseUsersJSON(result, mUserMap);

            // Something wrong with the JSON returned.
            if (result != null) {
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // Attempt to log in.
            if (!mUserMap.containsKey(mUsername)) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "User doesn't exist!",
                        Toast.LENGTH_SHORT).show();

            } else if (!mUserMap.get(mUsername).equals(mPassword)) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Invalid Username/Password combo!",
                        Toast.LENGTH_SHORT).show();

            } else {
                mSharedPreferences
                        .edit()
                        .putBoolean(getString(R.string.LOGGEDIN), true)
                        .commit();
                Intent intentShelf = new Intent(getActivity(), ShelfActivity.class);
                startActivity(intentShelf);
                getActivity().finish();

            }
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface LoginInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


}
