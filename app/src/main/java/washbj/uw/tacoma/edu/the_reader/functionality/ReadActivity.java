package washbj.uw.tacoma.edu.the_reader.functionality;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.authentication.LoginActivity;

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

    private float mLineSpacingMult = 1.2f;
    private float mLineSpacingExt = 14.0f;
    private float mTextSize = 20;
    private Typeface mTypeface = Typeface.MONOSPACE;

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
        super.onActivityResult(requestCode, resultCode, data);

        if  (requestCode == FILE_PICKED_RESULT) {
            if  (resultCode == RESULT_OK) {
                Uri fileURI = data.getData();
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
     * Loads in a file from some path, converting it to a
     * string and sending it off to the book-building methods.
     *
     * @param theFilePath The path to load the file from.
     */
    private void loadFile(String theFilePath) {
        Log.i("FILENAME", theFilePath);

        Log.e("storage", "External Storage State = " + Environment.getExternalStorageState());

        if (theFilePath.contains(".txt")) {
            BufferedReader brInput;
            StringBuilder sbOutput = new StringBuilder();
            File file = new File(theFilePath);

            try {
                if (file.exists()) {
                    brInput = new BufferedReader(new FileReader(file));
                    String sLine;

                    while ((sLine = brInput.readLine()) != null) {
                        sbOutput.append(sLine);

                    }

                    Log.e("notify", sbOutput.toString());
                    buildBook(sbOutput.toString());


                }

            } catch (FileNotFoundException exception) {
                Log.e("exception", exception.toString());
                Log.e("error", "--- ViewPageFragment could not open file [" + theFilePath + "]!");

            } catch (IOException exception) {
                Log.e("exception", exception.toString());
                Log.e("error", "--- ViewPageFragment could not read file [" + theFilePath + "]!");

            }

        } else {
            Log.e("error", "--- PATH [" + theFilePath + "]!");
            Toast toast = Toast.makeText(this, "File isn't a .txt!", Toast.LENGTH_SHORT);
            toast.show();

        }
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

}

