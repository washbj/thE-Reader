package washbj.uw.tacoma.edu.the_reader.functionality;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.authentication.LoginActivity;

public class ReadActivity extends AppCompatActivity
        implements PageFragment.OnFragmentInteractionListener {
    private static int FILE_PICKED_RESULT = 13126;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    PageFragment mPageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        checkPermissions();

        mPageFragment = new PageFragment();
        getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_read, mPageFragment)
                    .commit();

        this.findViewById(R.id.activity_read).setOnTouchListener(new OnSwipeTouchListener( this.findViewById(R.id.activity_read).getContext()) {
            @Override
            public void onSwipeLeft() {
                // Use to go to next page
                Log.w("SWIPE", "Swiped Left");

                mPageFragment.flipForward();

            }

            @Override
            public void onSwipeRight() {
                // Use to go to next page
                Log.w("SWIPE", "Swiped Right");

                mPageFragment.flipBack();

            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int iD = item.getItemId();
        // http://stackoverflow.com/questions/16894614/android-select-a-file-from-file-explorer
        if  (iD == R.id.open_file) {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("file/*.txt");
            startActivityForResult(fileIntent, FILE_PICKED_RESULT);
            return true;

        } else if (iD == R.id.action_logout) {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(getString(R.string.LOGIN_PREFS), Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(getString(R.string.LOGGEDIN), false)
                    .commit();

            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if  (requestCode == FILE_PICKED_RESULT) {
            if  (resultCode == RESULT_OK) {
                Uri fileURI = data.getData();

                mPageFragment.updateFile(fileURI);

            }

        }

    }

    private void checkPermissions() {
        String[] saPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

        int iPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (iPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, saPermissions, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
