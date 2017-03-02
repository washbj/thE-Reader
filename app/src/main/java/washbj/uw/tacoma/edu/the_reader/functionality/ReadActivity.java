package washbj.uw.tacoma.edu.the_reader.functionality;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import washbj.uw.tacoma.edu.the_reader.Data.BookDB;
import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.authentication.LoginActivity;

import static android.icu.util.ULocale.getName;

/**
 * The main activity to view the book's text. Also has a menu bar to log out
 * and open books.
 */
public class ReadActivity extends AppCompatActivity
        implements  ViewPageFragment.OnFragmentInteractionListener {

    /**
     * The int to represent a file has been picked
     */
    private static int FILE_PICKED_RESULT = 13126;
    /**
     * The int to represent a request on external storage
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    /**
     * The url of the book add php code
     */
    private static final String ADD_BOOK_URL ="http://cssgate.insttech.washington.edu/~_450bteam5/addBook.php?";

    /**
     * Code to show a file is being opened
     */
    private static final String OPEN_BOOK = "open";

    /**
     * Code to show a file is being closed or needs to be saved
     */
    private static final String  CLOSE_BOOK = "close";
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    /**
     * The SQLite database to store book info
     */
    private BookDB mBookDB;

    private float mLineSpacingMult = 1.2f;
    private float mLineSpacingExt = 14.0f;
    private float mTextSize = 20;
    private Typeface mTypeface = Typeface.MONOSPACE;


    /**
     * Array of strings to represent each page of a book
     */
    private String[] mPages;

    /**
     * The current page number
     */
    private int mPageNUmber;

    /**
     * The name of the file
     */
    private String mFileName;

    /**
     * The complete file path
     */
    private String mFIleLocation;


    /**
     * Creates the Activity and sets up the fragment
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        checkPermissions();

        mPages = new String[] {""};

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.page_viewer);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

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
        if  (iD == R.id.open_file) {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("*/*");
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

    /**
     * Checks whether the file is in standard format or a special format requiring
     * working around the actual path. Sends either the path for normal format files
     * or the actual book text to the ViewPageFragment. (passing book text is currently in need
     * of optimization)
     * @param requestCode
     * @param resultCode
     * @param data the file picked
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fullPath = "";
        Uri uri;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKED_RESULT) {
            if (resultCode == RESULT_OK) {

                uri = data.getData();
                String mimeType = getContentResolver().getType(uri);
                if (mimeType == null) {
                    fullPath = getPath(this, uri);
                    if (fullPath == null) {
                        mFileName = getName(uri.toString());
                    } else {
                        File file = new File(fullPath);
                        mFileName = file.getName();
                    }
                } else {
                    Uri returnUri = data.getData();
                    Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    mFileName = returnCursor.getString(nameIndex);
                    String size = Long.toString(returnCursor.getLong(sizeIndex));
                }
                try {
                    mFIleLocation = URLEncoder.encode(uri.toString(), "UTF-8");
                }catch (Exception e) {
                    Log.e("FileLoad", e.getMessage());
                }
                Log.i("FileLoad:", mFileName + "         " + mFIleLocation);
                //loadFile(uri);
                try {
                    readTextFromUri(Uri.parse(uri.toString()));
                   // readTextFromUri(uri);
                } catch (Exception e) {
                    Log.e("FileLoad", e.getMessage());
                }


            }

        }
    }

    /**
     * Checks to make sure we can read and write before doing so
     */
    private void checkPermissions() {
        String[] saPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

        int iPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (iPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, saPermissions, REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * Gets the text from the chosen file and passes it to the book builder.
     * Also adds the book name to the database
     * @param uri the uri of the chosen file
     * @throws IOException
     */

    private void readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        reader.close();
        addBook();
        bufferPages(mFileName, stringBuilder.toString());
    }

    /**
     * Adds the book info to the SQL server
     */
    public void addBook() {
        AddBookTask task = new AddBookTask();
        SaveBookProgress saveTask = new SaveBookProgress();
        saveTask.execute(new String[]{OPEN_BOOK});
        task.execute(new String[]{buildBookUrl()});
    }


    /**
     * Stores relevant book info when the user is done reading.
     * Mainly for the sake of restoring the page number.
     */
    @Override
    public void onStop() {
        if (mFIleLocation != null) {
            SaveBookProgress saveTask = new SaveBookProgress();
            mPageNUmber = mPager.getCurrentItem();
            saveTask.execute(new String[]{CLOSE_BOOK});
            Log.i("OnStop", "" + mPageNUmber);
        }
        super.onStop();
    }

    /**
     * Closes the database when destroyed.
     */
    @Override
    public void onDestroy() {
       // SaveBookProgress saveTask = new SaveBookProgress();
       // saveTask.execute(new String[]{CLOSE_BOOK});
        //Toast.makeText(this, "Saved book on page: " + mPageNUmber, Toast.LENGTH_LONG);
        mBookDB.closeDB();
        Log.i("OnDestroy", "" + mPageNUmber);
        super.onDestroy();
    }



    /**
     * New page buffer mechanic for splitting text into pages.
     *
     * @param theInputText The text to convert.
     */
    private void bufferPages(String theTitle, String theInputText) {
        ArrayList<String> alReturn = new ArrayList<String>();
        alReturn.add(theTitle);

        SpannableStringBuilder ssBuilder = new SpannableStringBuilder();
        ssBuilder.append(theInputText);
        Point pointSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(pointSize);

        TextPaint tpText = new TextPaint();
        tpText.setTextSize(mTextSize);
        tpText.setTypeface(mTypeface);

        StaticLayout layoutStatic = new StaticLayout(ssBuilder, tpText,
                pointSize.x * 5 / 6, Layout.Alignment.ALIGN_NORMAL, mLineSpacingMult, mLineSpacingExt, false);

        int iStartingLine = 0;

        while   (iStartingLine < layoutStatic.getLineCount()) {
            int startLineTop = layoutStatic.getLineTop(iStartingLine);
            int endLine = layoutStatic.getLineForVertical(startLineTop + pointSize.y / 3);
            int endLineBottom = layoutStatic.getLineBottom(endLine);
            int lastFullyVisibleLine;

            if  (endLineBottom > startLineTop + (pointSize.y / 3)) {
                lastFullyVisibleLine = endLine - 1;
            } else {
                lastFullyVisibleLine = endLine;
            }

            int startOffset = layoutStatic.getLineStart(iStartingLine);
            int endOffset = layoutStatic.getLineEnd(lastFullyVisibleLine);
            alReturn.add(ssBuilder.subSequence(startOffset, endOffset).toString());
            iStartingLine = lastFullyVisibleLine + 1;

        }

        mPages = alReturn.toArray(new String[alReturn.size()]);
        mPagerAdapter.notifyDataSetChanged();
        mPager.setAdapter(mPagerAdapter);

        mPager.postDelayed(new Runnable() {

            //Requires delay so that the page fragment can be built first
            @Override
            public void run() {
                Log.i("CUrrentPageToBEOPened", "" + mPageNUmber);
                mPager.setCurrentItem(mPageNUmber);
            }
        }, 100);
        //mPager.setCurrentItem(mPageNUmber);


    }

    /**
     * Default needed method
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

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

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ViewPageFragment page = ViewPageFragment.newInstance(mPages[position], mTextSize, mTypeface);

            return page;
        }

        @Override
        public int getCount() {
            return mPages.length;
        }

    }

    /**
     * Method that looks at the file path if it is in a Android folder that renames the file.
     * Returns the actual file name instead of something like: Doccument/555
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


    /**
     * Builds the book url with the filename
     * @return the php url.
     */
    public String buildBookUrl() {

        StringBuilder sb = new StringBuilder(ADD_BOOK_URL);

        try {
            sb.append("title=");
            sb.append(URLEncoder.encode(mFileName, "UTF-8"));

             Log.i("ADDBOOKURL", sb.toString());

             }
        catch(Exception e) {
            Toast.makeText(this,  "Cannot connect with database: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
         return sb.toString();
    }

    /**
     * Saves the books info to the sqlite database.
     */
    private class SaveBookProgress extends AsyncTask<String, Void, String> {

        /**
         * Records fileLocation, page and FileName to the database
         * @param params Close or Open code
         * @return generic info string.
         */
        @Override
        protected String doInBackground(String... params) {
            if (mBookDB == null) {
                mBookDB = new BookDB(getApplicationContext());

            }
            if (!mBookDB.CheckIsBookAlreadyInDBorNot(mFIleLocation)) {
                mPageNUmber = 0;
                mBookDB.insertBook(mFIleLocation, mPageNUmber, mFileName);
            } else {
                if (params[0].equals(OPEN_BOOK)) {
                    mPageNUmber = mBookDB.CheckPageNumber(mFIleLocation);
                } else if ( params[0].equals(CLOSE_BOOK)) {
                    mBookDB.updateBook(mFIleLocation, mPageNUmber, mFileName);
                }
            }
            return "Attempting to store user info:" + mFIleLocation + " "
            + " " + mPageNUmber + "  " + mFileName;
        }
    }


    /**
     * A class to add book data to the SQL server
     */
        private class AddBookTask extends AsyncTask<String, Void, String> {


        /**
         * Attempts to add the data to the server with the given url
         * @param urls the url with the data and php command name
         * @return the result
         */
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
                    response = "Unable to add book, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                 }
            }
            return response;
        }
    /**
     * It checks to see if there was a problem with the URL(Network) which is when an
     * exception is caught. It tries to call the parse Method and checks to see if it was successful.
     * If not, it displays the exception.
     *
     * @param result
     */
        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                    String status = (String) jsonObject.get("result");
                    if (status.equals("success")) {
                        Toast.makeText(getApplicationContext(),  "Book successfully added!" , Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to add: " + jsonObject.get("error")
                                        , Toast.LENGTH_LONG)
                                        .show();
                    }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Book's info already recorded", Toast.LENGTH_SHORT).show();
            }
        }
    }

}


