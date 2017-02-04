package washbj.uw.tacoma.edu.the_reader;

import android.Manifest;
import android.app.Activity;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PageFragment extends Fragment {
    private static final int SCREEN_WIDTH = 200;
    private static final int SCREEN_HEIGHT = 600;
    private static final int TEXT_SIZE = 20;

    private OnFragmentInteractionListener mListener;
    String sInputText;
    TextView mPageText;
    ArrayList<String> sPages = new ArrayList<String>();
    int iPage = 0;

    public PageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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

    public void flipForward() {
        if (iPage < sPages.size() - 1) {
            iPage++;

            mPageText.setText(sPages.get(iPage));

        }

    }

    public void flipBack() {
        if (iPage > 0) {
            iPage--;
            mPageText.setText(sPages.get(iPage));

        }

    }

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
     * Carves an input text file into an ArrayList of "pages": chunks of words the right size to fit
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
