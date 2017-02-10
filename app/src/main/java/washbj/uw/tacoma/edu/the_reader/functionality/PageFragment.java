package washbj.uw.tacoma.edu.the_reader.functionality;

import android.content.Context;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import washbj.uw.tacoma.edu.the_reader.R;

/**
 * A Fragment for breaking input text up into "pages",
 * such that each "page" can be displayed fully within
 * the confines of a single screen. Pages may be flipped
 * forwards and backwards as well.
 */
public class PageFragment extends Fragment {
    /** Hard-coded variable for the screen width to use when calculating how much text will fit. */
    private static final int SCREEN_WIDTH = 200;

    /** Hard-coded variable for the screen height to use when calculating how much text will fit. */
    private static final int SCREEN_HEIGHT = 600;

    /** The desired size of the text. */
    private static final int TEXT_SIZE = 20;

    /** Unused. */
    private OnFragmentInteractionListener mListener;

    /** The input text to convert to pages. */
    String sInputText;

    /** The TextView used to print pages to the screen. */
    TextView mPageText;

    /** An ArrayList of all pages, stored as Strings. */
    ArrayList<String> sPages = new ArrayList<String>();

    /** The current page. */
    int iPage = 0;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }

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
        if (iPage < sPages.size() - 1) {
            iPage++;

            mPageText.setText(sPages.get(iPage));

        }

    }

    /**
     * Flips back one page in the series. Will not flip if at the beginning page.
     */
    public void flipBack() {
        if (iPage > 0) {
            iPage--;
            mPageText.setText(sPages.get(iPage));

        }

    }

    /**
     * Reads in text from some input file, converting it to pages and pushing them to the ArrayList.
     *
     * @param theFileUri The URI passed in from ReadActivity, pointing to the .txt file to read from.
     */
    public void updateFile(Uri theFileUri) {
        String filePath = theFileUri.getPath();

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

                    iPage = 0;
                    sPages = pagesFromString(sbOutput.toString(), SCREEN_HEIGHT, SCREEN_WIDTH);
                    mPageText.setText(sPages.get(iPage));

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
        int iChars = 0;
        String sWords = theInputWords;

        ArrayList<String> sReturn = new ArrayList<String>();
        int iEnd = 0;

        while  (sWords.length() > 0) {
            for (int iCount = 0; iCount < iMaxLines; iCount++) {
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
}
