package washbj.uw.tacoma.edu.the_reader.functionality;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import washbj.uw.tacoma.edu.the_reader.Data.BookDB;
import washbj.uw.tacoma.edu.the_reader.R;

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


    private String[] test = {"Testificate 1", "Testificate 2", "Testificate 3"};

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

    private SharedPreferences mShelfSharedPreferences;
    private SharedPreferences mVisualSharedPreferences;

    /**
     * The SQLite database to store book info
     */
    private BookDB mBookDB;

    public static final float[]  TEXT_SIZES = {14, 16, 18, 20, 22, 24, 26};
    public static final Typeface[] TYPEFACES = {Typeface.MONOSPACE, Typeface.SERIF, Typeface.SANS_SERIF};
    public static final int[] BACKGROUNDS = {R.drawable.parchment_gradient, R.drawable.paper_gradient, R.drawable.gold_gradient};

    private float mTextSize;
    private int mTypeface;
    private float mLineSpacingMult = 1.2f;
    private float mLineSpacingExt = 14.0f;

    private int mPosition;

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
     * The title of the book.
     */
    private String mTitle;

    /**
     * The complete file path
     */
    private String mFileLocation;


    private int mBackground;


    /**
     * Creates the Activity and sets up the fragment
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mPosition = getIntent().getIntExtra("position", 0);

        mShelfSharedPreferences = getSharedPreferences(getString(R.string.BOOK_SHELF), Context.MODE_PRIVATE);

        mFileName = mShelfSharedPreferences.getString(getString(R.string.BOOK_TAG) + mPosition + "_filename", "FILE_NAME");
        mTitle = mShelfSharedPreferences.getString(getString(R.string.BOOK_TAG) + mPosition + "_title", "BOOK_TITLE");
        mFileLocation = mShelfSharedPreferences.getString(getString(R.string.BOOK_TAG) + mPosition + "_location", "FILE_LOCATION");

        mVisualSharedPreferences = getSharedPreferences(getString(R.string.VISUAL_PREFS), Context.MODE_PRIVATE);
        mTextSize = TEXT_SIZES[mVisualSharedPreferences.getInt(getString(R.string.VP_TEXTSIZE) + mPosition, 0)];
        mTypeface = mVisualSharedPreferences.getInt(getString(R.string.VP_TYPEFACE) + mPosition, 0);

        mBackground = BACKGROUNDS[mVisualSharedPreferences.getInt(getString(R.string.VP_BACKGROUND) + mPosition, 0)];
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_read);
        layout.setBackgroundResource(mBackground);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.page_viewer);

        try {
            readTextFromUri(Uri.parse(mFileLocation));
        } catch (IOException e) {
            Log.e("ReadActivity.onCreate()", "Unable to parse file location " + mFileLocation);
            e.printStackTrace();
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
        BufferPagesTask bufferPages = new BufferPagesTask();
        bufferPages.execute(stringBuilder.toString());
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
        if (mFileLocation != null) {
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
        if (mBookDB != null) {
            //Currently commented out because the flow of the application was changed
           // mBookDB.closeDB();
            Log.i("OnDestroy", "" + mPageNUmber);
        }

        super.onDestroy();
    }



    /**
     * New page buffer mechanic for splitting text into pages.
     *
     * @param theInputText The text to convert.
     */
    private String[] bufferPages(String theInputText) {
        ArrayList<String> alReturn = new ArrayList<String>();

        SpannableStringBuilder ssBuilder = new SpannableStringBuilder();
        ssBuilder.append(theInputText);
        Point pointSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(pointSize);

        TextPaint tpText = new TextPaint();
        tpText.setTextSize(mTextSize);
        tpText.setTypeface(TYPEFACES[mTypeface]);

        float fDensity = getResources().getDisplayMetrics().density;

        StaticLayout layoutStatic = new StaticLayout(ssBuilder, tpText,
                (int) (pointSize.x / fDensity), Layout.Alignment.ALIGN_NORMAL,
                mLineSpacingMult, mLineSpacingExt, false);

        int iStartingLine = 0;

        while   (iStartingLine < layoutStatic.getLineCount()) {
            int startLineTop = layoutStatic.getLineTop(iStartingLine);
            int endLine = layoutStatic.getLineForVertical(startLineTop + (int) (((pointSize.y * 7 / 8) / fDensity)));
            int endLineBottom = layoutStatic.getLineBottom(endLine);
            int lastFullyVisibleLine;

            if  (endLineBottom > startLineTop + ((pointSize.y * 7 / 8) / fDensity)) {
                lastFullyVisibleLine = endLine - 1;
            } else {
                lastFullyVisibleLine = endLine;
            }

            int startOffset = layoutStatic.getLineStart(iStartingLine);
            int endOffset = layoutStatic.getLineEnd(lastFullyVisibleLine);
            alReturn.add(ssBuilder.subSequence(startOffset, endOffset).toString());
            iStartingLine = lastFullyVisibleLine + 1;

        }

        return alReturn.toArray(new String[alReturn.size()]);

    }

    /**
     * Default needed method
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("ScreenSlidePagerAdapter", " --- Position = " + position);
            Log.d("ScreenSlidePagerAdapter", " --- Text = " + mPages[position]);
            Log.d("CurrentPagetobeOpened", "" + mPageNUmber);

            ViewPageFragment page = new ViewPageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("text", mPages[position]);
            bundle.putFloat("text_size", mTextSize);
            bundle.putInt("typeface", mTypeface);
            bundle.putInt("page_num", position + 1);
            page.setArguments(bundle);

            return page;

        }

        @Override
        public int getCount() {
            return mPages.length;
        }

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
            if (!mBookDB.CheckIsBookAlreadyInDBorNot(mFileLocation)) {
                mPageNUmber = 0;
                mBookDB.insertBook(mFileLocation, mPageNUmber, mFileName);
            } else {
                if (params[0].equals(OPEN_BOOK)) {
                    mPageNUmber = mBookDB.CheckPageNumber(mFileLocation);
                } else if ( params[0].equals(CLOSE_BOOK)) {
                    mBookDB.updateBook(mFileLocation, mPageNUmber, mFileName);
                }
            }
            return "Attempting to store user info:" + mFileLocation + " "
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


    private class BufferPagesTask extends AsyncTask<String, Void, String> {
        String[] pageBuffer;

        @Override
        protected String doInBackground(String... urls) {
            for (String inputText : urls) {
                pageBuffer = bufferPages(inputText);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            mPages = pageBuffer;

            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPagerAdapter);
            mPager.setCurrentItem(mPageNUmber);

//            mPager.postDelayed(new Runnable() {
//
//                //Requires delay so that the page fragment can be built first
//                @Override
//                public void run() {
//                    Log.i("CurrentPagetobeOpened", "" + mPageNUmber);
//                    mPager.setCurrentItem(mPageNUmber);
//                }
//            }, 100);
//            mPager.setCurrentItem(mPageNUmber);

        }
    }

}