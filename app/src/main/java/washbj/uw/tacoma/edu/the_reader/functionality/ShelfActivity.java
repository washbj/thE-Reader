package washbj.uw.tacoma.edu.the_reader.functionality;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.authentication.LoginActivity;

import static android.icu.util.ULocale.getName;

/**
 * The main activity to view the book's text. Also has a menu bar to log out
 * and open books.
 */
public class ShelfActivity extends AppCompatActivity
        implements  ViewBookFragment.OnFragmentInteractionListener,
                    AddBookFragment.OnFragmentInteractionListener {

    /**
     * The int to represent a file has been picked
     */
    public static int FILE_PICKED_RESULT = 342;

    /**
     * The int to represent a file has been picked
     */
    public static int SETTINGS_UPDATED_RESULT = 731;

    /**
     * The int to represent a request on external storage
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    /**
     * The url of the book add php code
     */
    private static final String ADD_BOOK_URL ="http://cssgate.insttech.washington.edu/~_450bteam5/addBook.php?";

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private SharedPreferences mShelfSharedPreferences;

    /**
     * Creates the Activity and sets up the fragment
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf);

        checkPermissions();

        mShelfSharedPreferences = getSharedPreferences(getString(R.string.BOOK_SHELF)
                , Context.MODE_PRIVATE);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.book_viewer);
        mPagerAdapter = new BookPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

    }


    /**
     * Default needed method
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /**
     * Checks to make sure we can read and write before doing so
     */
    private void checkPermissions() {
        String[] saPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        int iPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (iPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, saPermissions, REQUEST_EXTERNAL_STORAGE);
        }
    }


    /**
     * Creates the option menu
     * @param menu
     * @return always true
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;

    }

    /**
     * Opens a file and sends it to the logic methods or logs the user out.
     * @param item the menu item that was selected
     * @return true if the operation was a success
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int iD = item.getItemId();
        // http://stackoverflow.com/questions/16894614/android-select-a-file-from-file-explorer
        if  (iD == R.id.clear_list) {
            clearShelf();
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


    public void selectFile() {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, FILE_PICKED_RESULT);
    }


    /**
     * If the user is currently looking at the first step, allow the system to handle the
     * Back button. This calls finish() on this activity and pops the back stack.
     * Otherwise, select the previous step.
     */
    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void updatePageView() {
        mPagerAdapter.notifyDataSetChanged();
        mPager.setAdapter(mPagerAdapter);
    }

    public void openSettings() {
        Intent intentSettings = new Intent(this, SettingsActivity.class);
        intentSettings.putExtra("position", mPager.getCurrentItem());
        startActivityForResult(intentSettings, ShelfActivity.SETTINGS_UPDATED_RESULT);
    }

    private class BookPagerAdapter extends FragmentStatePagerAdapter {
        public BookPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int iLength = mShelfSharedPreferences.getInt(getString(R.string.BOOK_SHELF_COUNT), 0);
            Log.e("BookPagerAdapter", "--- iLength = " + iLength);

            if  (position == iLength) {
                return AddBookFragment.newInstance();

            } else {
                ViewBookFragment vbf = ViewBookFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                vbf.setArguments(bundle);
                return vbf;

            }

        }

        @Override
        public int getCount() {
            return mShelfSharedPreferences.getInt(getString(R.string.BOOK_SHELF_COUNT), 0) + 1;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fullPath = "";
        Uri uri;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKED_RESULT) {
            if (resultCode == RESULT_OK) {
                String sFileName = "";
                String sFileLocation = "";

                uri = data.getData();
                String mimeType = getContentResolver().getType(uri);
                if (mimeType == null) {
                    fullPath = getPath(this, uri);
                    if (fullPath == null) {
                        sFileName = getName(uri.toString());
                    } else {
                        File file = new File(fullPath);
                        sFileName = file.getName();
                    }
                } else {
                    Uri returnUri = data.getData();
                    Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    sFileName = returnCursor.getString(nameIndex);
                    String size = Long.toString(returnCursor.getLong(sizeIndex));
                }
                try {
                    sFileLocation = URLEncoder.encode(uri.toString(), "UTF-8");
                }catch (Exception e) {
                    Log.e("FileLoad", e.getMessage());
                }
                Log.i("FileLoad:", sFileName + "         " + sFileLocation);
                //loadFile(uri);
                try {
                    addBook(sFileName, uri.toString());
                    // readTextFromUri(uri);
                } catch (Exception e) {
                    Log.e("FileLoad", e.getMessage());
                }


            }

        } else if (requestCode == SETTINGS_UPDATED_RESULT) {
                recreate();
        }

    }

    private void clearShelf() {
        SharedPreferences.Editor spEditor = mShelfSharedPreferences.edit();
        spEditor.putInt(getString(R.string.BOOK_SHELF_COUNT), 0);
        spEditor.commit();

        Toast.makeText(this, "Books cleared!", Toast.LENGTH_SHORT);

        updatePageView();
        mPager.postDelayed(new Runnable() {

            //Requires delay so that the page fragment can be built first
            @Override
            public void run() {
                mPager.setCurrentItem(0);
            }
        }, 100);

    }

    private void addBook(String theName, String theLocation) {
        int iPosition = mShelfSharedPreferences.getInt(getString(R.string.BOOK_SHELF_COUNT), 0);
        SharedPreferences.Editor spEditor = mShelfSharedPreferences.edit();
        spEditor.putString(getString(R.string.BOOK_TAG) + iPosition + "_title", theName);
        spEditor.putString(getString(R.string.BOOK_TAG) + iPosition + "_filename", theName);
        spEditor.putString(getString(R.string.BOOK_TAG) + iPosition + "_location", theLocation);
        spEditor.putInt(getString(R.string.BOOK_SHELF_COUNT), iPosition + 1);
        spEditor.commit();

        Toast.makeText(this, "Book added!", Toast.LENGTH_SHORT);

        updatePageView();
        mPager.postDelayed(new Runnable() {

            //Requires delay so that the page fragment can be built first
            @Override
            public void run() {
                mPager.setCurrentItem(0);
            }
        }, 100);

    }


    /**
     * Method that looks at the file path if it is in a Android folder that renames the file.
     * Returns the actual file name instead of something like: Document/555
     * @param context current activity
     * @param uri the file path
     * @return the actual file name
     */
    public static String getPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}


