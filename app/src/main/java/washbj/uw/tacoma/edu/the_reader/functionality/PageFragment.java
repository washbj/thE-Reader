package washbj.uw.tacoma.edu.the_reader.functionality;
/**
 * @Author Michael Scott, Justin Washburn on 2/11/2017.
 */
import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import washbj.uw.tacoma.edu.the_reader.R;

/**
 * A Fragment for breaking input text up into "pages",
 * such that each "page" can be displayed fully within
 * the confines of a single screen. Pages may be flipped
 * forwards and backwards as well. Also manages the storing
 * of relevant book data in the SQL server.
 */
public class PageFragment extends Fragment {

    /**
     * The url of the book add php code
     */
    private static final String ADD_BOOK_URL ="http://cssgate.insttech.washington.edu/~_450bteam5/addBook.php?";

    /** Hard-coded variable for the screen width to use when calculating how much text will fit. */
    private static final int SCREEN_WIDTH = 200;

    /** Hard-coded variable for the screen height to use when calculating how much text will fit. */
    private static final int SCREEN_HEIGHT = 600;

    /** The desired size of the text. */
    private static final int TEXT_SIZE = 20;

    /** Unused. */
    private OnFragmentInteractionListener mListener;

    /** The input text to convert to pages. */
    String mInputText;

    /** The TextView used to print pages to the screen. */
    TextView mPageText;

    /** An ArrayList of all pages, stored as Strings. */
    ArrayList<String> mPages = new ArrayList<>();

    /** The current page. */
    int mPageNumber = 0;

    public PageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment PageFragment.
     */
    public static PageFragment newInstance() {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_page, container, false);

        mPageText = (TextView) view.findViewById(R.id.page_text);
        mPageText.setTextSize(TEXT_SIZE);

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Flips to the next page in the series. Will not flip if there are no more pages.
     */
    public void flipForward() {
        if (mPageNumber < mPages.size() - 1) {
            mPageNumber++;

            mPageText.setText(mPages.get(mPageNumber));

        }

    }

    /**
     * Flips back one page in the series. Will not flip if at the beginning page.
     */
    public void flipBack() {
        if (mPageNumber > 0) {
            mPageNumber--;
            mPageText.setText(mPages.get(mPageNumber));

        }

    }

    /**
     * Reads in text from some input file, converting it to pages and pushing them to the ArrayList.
     *
     * @param filePath The String passed in from ReadActivity, pointing to the .txt file to read from.
     */
    public void updateFile(String filePath) {

        Log.i("FILENAME", filePath);

        Log.e("storage", "External Storage State = " + Environment.getExternalStorageState());

        if (filePath.contains(".txt")) {
            BufferedReader brInput;
            StringBuilder sbOutput = new StringBuilder();
            File file = new File(filePath);

            try {
                if (file.exists()) {
                    brInput = new BufferedReader(new FileReader(file));
                    String sLine;

                    while ((sLine = brInput.readLine()) != null) {
                        sbOutput.append(sLine);

                    }

                    Log.e("notify", sbOutput.toString());
                    startPage(sbOutput.toString());


                }

            } catch (FileNotFoundException exception) {
                Log.e("exception", exception.toString());
                Log.e("error", "--- PageFragment could not open file [" + filePath + "]!");

            } catch (IOException exception) {
                Log.e("exception", exception.toString());
                Log.e("error", "--- PageFragment could not read file [" + filePath + "]!");

            }

        } else {
            Log.e("error", "--- PATH [" + filePath + "]!");
            Toast toast = Toast.makeText(getActivity(), "File isn't a .txt!", Toast.LENGTH_SHORT);
            toast.show();

        }

    }

    /**
     * Both starts the reading activity by loading in the first page
     * and record relevant book info to the SQL server.
     * @param theBook The book to start reading.
     */
    public void startPage(String theBook) {
        String bookTitle = "";
        if (theBook.length() > 30) {
            bookTitle = theBook.substring(0, 30);
        }
        String bookUrl = buildBookUrl(bookTitle);
        addBook(bookUrl);

        addBook(bookUrl);
        mPageNumber = 0;
        mPages = pagesFromString(theBook, SCREEN_HEIGHT, SCREEN_WIDTH);
        mPageText.setText(mPages.get(mPageNumber));
    }



    /**
     * Carves an input String into an ArrayList of "pages": chunks of words the right size to fit
     * within a given area.
     *
     * @param theInputWords The words to cut up.
     * @param theBoundingHeight The height of the page.
     * @param theBoundingWidth The width of the page.
     * @return Returns an ArrayList<String>
     */
    private ArrayList<String> pagesFromString(String theInputWords, int theBoundingHeight, int theBoundingWidth) {
        Paint painter = new Paint();
        painter.setTextSize(TEXT_SIZE);
        int iMaxLines = theBoundingHeight / TEXT_SIZE;
        int iChars;
        String sWords = theInputWords;

        ArrayList<String> sReturn = new ArrayList<>();
        int iEnd = 0;

        // Loops through text, splitting it into page-sized pieces.
        while  (sWords.length() > 0) {
            Log.e("pagecount", "Words left: " + sWords.length());
            for (int iCount = 0; iCount < iMaxLines; iCount++) {
                Log.e("forloop", "Mark");
                iChars = painter.breakText(sWords.substring(iEnd), true, theBoundingWidth, null);

                if (iEnd + iChars < sWords.length()) {
                    iEnd = iEnd + iChars;
                } else {
                    iEnd = sWords.length();
                    break;
                }

            }

            while   (iEnd < sWords.length() && sWords.charAt(iEnd) != ' ') {
                iEnd++;
            }

            sReturn.add(sWords.substring(0, iEnd));
            sWords = sWords.substring(iEnd);
            iEnd = 0;

        }

        return sReturn;

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    public String buildBookUrl(String bookTitle) {

        StringBuilder sb = new StringBuilder(ADD_BOOK_URL);

        try {
            sb.append("title=");
            sb.append(URLEncoder.encode(bookTitle, "UTF-8"));


            Log.i("ADDBOOKURL", sb.toString());

        }
        catch(Exception e) {
            Toast.makeText(getContext(), "Cannot connect with database: " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }

        return sb.toString();
    }

    /**
     * Adds the book info to the SQL server
     * @param url the url required to add book info
     */
    public void addBook(String url) {

        AddBookTask task = new AddBookTask();
        task.execute(new String[]{url});
    }

    /**
     * A class to add book data to the server while the user reads
     */
    private class AddBookTask extends AsyncTask<String, Void, String> {


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
                    String s;
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
         * @param result The incoming JSON info to parse.
         */
        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = (String) jsonObject.get("result");
                if (status.equals("success")) {
                    Toast.makeText(getContext(), "Book successfully added!"
                            , Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(getContext(), "Failed to add: "
                                    + jsonObject.get("error")
                            , Toast.LENGTH_LONG)
                            .show();
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Book's info already recorded", Toast.LENGTH_SHORT).show();
            }
        }


    }

}
