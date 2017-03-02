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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private String filename;

    private String[] mPages;

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
     * @param data
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
                        filename = getName(uri.toString());
                    } else {
                        File file = new File(fullPath);
                        filename = file.getName();
                    }
                } else {
                    Uri returnUri = data.getData();
                    Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    filename = returnCursor.getString(nameIndex);
                    String size = Long.toString(returnCursor.getLong(sizeIndex));
                }


                if (mBookDB == null) {
                    mBookDB = new BookDB(this);
                }
                //mBookDB.deleteBooks();
                Log.i("LoadBook", filename + "      " + uri.toString());

                //always sets page to 0
               /* if (!mBookDB.CheckIsBookAlreadyInDBorNot(filename)) {
                    mBookDB.insertBook(filename, 0);
                }*/
                //loadFile(uri);
                try {
                    readTextFromUri(Uri.parse(uri.toString()));
                   // readTextFromUri(uri);
                } catch (Exception e) {

                }


            }
      /*  super.onActivityResult(requestCode, resultCode, data);

        if  (requestCode == FILE_PICKED_RESULT) {
            if  (resultCode == RESULT_OK) {
                Uri fileURI = data.getData();
                Log.i("LoadBook", fileURI.toString());
                String filePath = fileURI.getPath();


                //for select devices that do not give the file path (Samsung):
                if (!filePath.contains(".txt")) {
                    try {
                        InputStream file = getContentResolver().openInputStream(fileURI);
                        java.util.Scanner s = new java.util.Scanner(file).useDelimiter("\\A");
                        filePath = s.hasNext() ? s.next() : "";
                        loadFile(filePath);
                    } catch (Exception e) {
                        Log.e("FileError", "Error opening file" + e);
                    }
                } else {
                    loadFile(filePath);
                }
            }
        }*/
        }
    }

    private void checkPermissions() {
        String[] saPermissions = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

        int iPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (iPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, saPermissions, REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * Gets the text from the chosen file and passes it to the book builder
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
        buildBook(stringBuilder.toString());
    }



    /**
     * Both starts the reading activity by loading in the first page
     * and record relevant book info to the SQL server.
     *
     * @param theInputString
     */
    private void buildBook(String theInputString) {
        String bookTitle = "";
        if (theInputString.length() > 30) {
            bookTitle = theInputString.substring(0, 30);
        }
        //String bookUrl = buildBookUrl(bookTitle);
        //addBook(bookUrl);

        bufferPages(bookTitle, theInputString);

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
        mPager.setCurrentItem(0);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

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


