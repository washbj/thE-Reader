package washbj.uw.tacoma.edu.the_reader.functionality;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import washbj.uw.tacoma.edu.the_reader.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPageFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    /** The text to display. */
    String mText;

    /** The desired size of the text. */
    float mTextSize;

    /** The desired typeface of the text. */
    Typeface mTypeface;

    /** The TextView used to print pages to the screen. */
    TextView mPageText;

    /** The TextView used to print pages to the screen. */
    TextView mPageNumber;

    public ViewPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewPageFragment.
     */
    public static ViewPageFragment newInstance() {
        ViewPageFragment fragment = new ViewPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_page, container, false);

        mText = "ERROR: NO BUNDLE FOUND";
        mTextSize = 16.0f;
        mTypeface = Typeface.MONOSPACE;
        int iPageNumber = 0;

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mText = bundle.getString("text", "Loading...");
            mTextSize = bundle.getFloat("text_size", 16.0f);
            mTypeface = ReadActivity.TYPEFACES[bundle.getInt("typeface", 0)];
            iPageNumber = bundle.getInt("page_num", 1);
        }

        mPageText = (TextView) view.findViewById(R.id.page_text);
        mPageText.setTextSize(mTextSize);
        mPageText.setTypeface(mTypeface);
        mPageText.setText(mText);

        mPageNumber = (TextView) view.findViewById(R.id.page_number);
        mPageNumber.setTextSize(mTextSize);
        mPageNumber.setTypeface(mTypeface);
        mPageNumber.setText("" + iPageNumber);

        return view;

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
